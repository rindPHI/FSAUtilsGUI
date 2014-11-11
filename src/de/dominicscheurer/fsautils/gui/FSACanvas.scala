/*
 * Copyright 2014 Dominic Scheurer
 * 
 * This file is part of FSAUtilsGUI.
 * 
 * FSAUtilsGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FSAUtilsGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with FSAUtilsGUI. If not, see <http://www.gnu.org/licenses/>.
 */

package de.dominicscheurer.fsautils.gui

import scala.swing.Panel
import java.awt.{ Graphics2D, Color, Point }
import java.awt.geom.Ellipse2D
import java.awt.Font
import java.awt.geom.QuadCurve2D
import scala.swing.Dialog
import javax.swing.JOptionPane
import java.awt.Dimension
import de.dominicscheurer.fsautils._
import de.dominicscheurer.fsautils.Types._
import de.dominicscheurer.fsautils.Conversions._
import scala.util.control.NonFatal
import scala.swing.event.MousePressed
import scala.swing.event.MouseDragged

class FSACanvas extends Panel {
    
    type DFAState = de.dominicscheurer.fsautils.Types.State

    var states = Set[State]()
    var selectedState = None: Option[State]
    var selectedEdge = None: Option[(State, String, State)]
    var initialState = None: Option[State]
    var edges = Set[(State, String, State)]()

    var nextStateCounter = 0
    var notPossibleStateHint = None: Option[State]

    val STATE_DIAMETER = 50
    val ACCPT_STATE_INNER_DIAMETER = 44
    val ACCPT_STATE_INNERST_DIAMETER = 20
    val BORDER_SIZE = STATE_DIAMETER
    
    val STD_FG_COLOR = Color.BLACK
    val SELECTED_FG_COLOR = Color.BLUE
    val STD_BG_COLOR = Color.WHITE
    val BORDER_BG_COLOR = Color.LIGHT_GRAY
    val IMPOSSIBLE_HINT_COLOR = Color.RED
    val INITIAL_STATE_COLOR = Color.GREEN
    
    val STD_FONT = new Font("SansSerif", Font.PLAIN, 20)
    val HINT_FONT = new Font("SansSerif", Font.PLAIN, 10)
    def INPUT_HINT_TEXT_POS(_size: Dimension) = (20, _size.height - 10)
    val INPUT_HINT_TEXT =
        "When a state is selected, press 'a' to make it accepting and 'i' to make it initial. " +
        "Hold Shift and click on an other state to add a transition."
        
    listenTo(mouse.moves)
    listenTo(mouse.clicks)
    
    reactions += {
        case MouseDragged(src, point, mods) => {
            val intersStates = statesAtPoint(point)
            if (notInOuterBorder(point) && intersStates.size == 1)
                (intersStates head) match {
                    case State(x, y, label, accepting) =>
                        val oldState = State(x, y, label, accepting)
                        val newState = State(point.x, point.y, label, accepting)
                        replaceState(oldState, newState)
                        selectedState = Some(newState)
                }
        }
    }

    override def paintComponent(g: Graphics2D) {
        g setColor BORDER_BG_COLOR
        g fillRect (0, 0, size.width, size.height)
        g setColor STD_BG_COLOR
        g fillRect (BORDER_SIZE, BORDER_SIZE, size.width - (2 * BORDER_SIZE), size.height - (2 * BORDER_SIZE))

        g setFont HINT_FONT
        g setColor STD_FG_COLOR
        
        g drawString (INPUT_HINT_TEXT, INPUT_HINT_TEXT_POS(size)._1, INPUT_HINT_TEXT_POS(size)._2)
        
        g setFont STD_FONT
        
        for (edge <- edges) {
            edge match {
                case (from, label, to) => {
                    val r = STATE_DIAMETER / 2

                    selectedEdge match {
                        case None => g setFont STD_FONT
                        case Some(otherEdge) =>
                            if (otherEdge equals edge)
                                g setColor SELECTED_FG_COLOR
                            else
                                g setColor STD_FG_COLOR
                    }
                    
                    if (!(from equals to)) {
                        var (x, y, xx, yy) = (from.x, from.y, to.x, to.y)

                        val (diffX, diffY) = (Math.abs(xx - x).toInt, Math.abs(yy - y).toInt)

                        val hypotenuse = Math.sqrt(
                            Math.pow(diffX, 2) +
                                Math.pow(diffY, 2))

                        var alpha = 0
                        if (diffX < r)
                            alpha = Math.asin(diffY / hypotenuse).toInt
                        else
                            alpha = Math.acos(diffX / hypotenuse).toInt

                        val offsetX = (r * Math.cos(alpha)).toInt
                        val offsetY = (r * Math.sin(alpha)).toInt

                        if (x < xx) {
                            x += offsetX
                            xx -= offsetX
                        } else {
                            x -= offsetX
                            xx += offsetX
                        }
                        if (y < yy) {
                            y += offsetY
                            yy -= offsetY
                        } else {
                            y -= offsetY
                            yy += offsetY
                        }

                        drawArrow(g, x, y, xx, yy)
                        
                        // draw label
                        val labelXDiff = (to.x - from.x) / 2
                        val labelYDiff = (to.y - from.y) / 2
                        g drawString (label, from.x + labelXDiff, from.y + labelYDiff)
                    } else {
                        // The edge is a loop
                        g draw getInnerOval(from.x + r, from.y - r)
                        drawArrow(g, from.x + r + 5, from.y - 4, from.x + r, from.y - 1)
                        
                        g setColor STD_BG_COLOR
                        g fill getOuterOval(from.x, from.y)
                        g setColor STD_FG_COLOR
                        
                        // draw label
                        val labelX = from.x + ACCPT_STATE_INNER_DIAMETER
                        val labelY = from.y - ACCPT_STATE_INNER_DIAMETER
                        g drawString (label, labelX, labelY)
                    }
                }
            }
        }

        for (state <- states) {
            g setColor STD_FG_COLOR

            selectedState match {
                case None =>
                case Some(otherState) =>
                    if (state equals otherState)
                        g setColor SELECTED_FG_COLOR
            }

            state match {
                case State(x: Int, y: Int, label: String, isAccepting: Boolean) => {

                    g.draw(getOuterOval(x, y))

                    if (isAccepting)
                        g.draw(getInnerOval(x, y))

                    initialState match {
                        case None =>
                        case Some(otherState) =>
                            if (otherState equals state) {
                                g setColor INITIAL_STATE_COLOR
                                g fill getInnerstOval(x, y)
                                g setColor STD_FG_COLOR
                            }
                    }

                    g.drawString(label, x - (5 * label.size), y + 7)

                }
            }
        }

        g.setColor(IMPOSSIBLE_HINT_COLOR)
        notPossibleStateHint match {
            case None        =>
            case Some(state) => notPossibleStateHint = None; g.draw(getOuterOval(state.x, state.y))
        }
    }

    def checkState(point: Point) {
        val state = State(point.getX.toInt, point.getY.toInt, nextStateCounter.toString, false)
        val intersectingStates = statesAtPoint(point)

        selectedState = None

        if (intersectingStates.isEmpty && notInOuterBorder(point)) {

            states += state
            nextStateCounter += 1
            selectedState = Some(state)

        } else {

            notPossibleStateHint = Some(state)

        }

        repaint
    }

    def checkSelect(point: Point) {
        selectedState = None
        selectedEdge = None

        val intersectingStates = statesAtPoint(point)
        if (intersectingStates.size == 1) {

            selectedState = Some(intersectingStates head)

        } else {
            val intersectingEdges = edgesAtPoint(point)
            if (intersectingEdges.size == 1) {
                
                selectedEdge = Some(intersectingEdges head)
                
            }
        }

        repaint
    }

    def checkEdge(point: Point) {
        val intersectingStates = statesAtPoint(point)

        selectedState match {
            case None =>
            case Some(otherState) =>
                if (intersectingStates.size == 1) {
                    Dialog.showInput(
                        this,
                        "On what input shall this transition occur?",
                        title="New Transition",
                        initial="") match {
                            case None =>
                            case Some(label) => edges += ((otherState, label, intersectingStates.head))
                        }
                }
        }

        repaint
    }

    def checkAccepting = {
        selectedState match {
            case None =>
            case Some(State(x, y, label, accepting)) => {
                val oldState = State(x, y, label, accepting)
                val newState = State(x, y, label, !accepting)

                replaceState(oldState, newState)
                selectedState = Some(newState)
            }
        }

        repaint
    }

    def checkInitial = {
        selectedState match {
            case None =>
            case _    => initialState = selectedState
        }

        repaint
    }

    def checkDelete = {
        selectedState match {
            case None =>
            case Some(state) => {
                selectedState = None
                selectedEdge = None
                states -= state
                edges = edges.filterNot{
                    case (from, _, to) =>
                        (from.equals(state) || to.equals(state))
                }
                initialState match {
                    case None =>
                    case Some(otherState) =>
                        if (state equals otherState)
                            initialState = None
                }
            }
        }
        
        selectedEdge match {
            case None =>
            case Some(edge) => {
                selectedState = None
                selectedEdge = None
                edges -= edge
            }
        }

        repaint
    }
    
    def fsm: Option[FSM] =
        // If there are less than states * alphabet transitions,
        // the result cannot be a DFA, so try NFA instead.
        if (states.size * alphabet.size > edges.size) {
            nfa
        } else {
            dfa match {
                case None => nfa
                case Some(dfa) => Some(dfa)
            }
        }
    
    def nfa: Option[NFA] = {
        try {
            val dfaStates = states.map { 
                case State(_, _, label, _) => q(label.toInt)
            }: Set[DFAState]
            
            val dfaInitialState = (initialState match {
                case None => error("No initial state selected!")
                case Some(state) => q(state.label.toInt)
            }): DFAState
            
            val dfaAccepting = ((states.filter { 
                case State(_, _, _, isAccepting) => isAccepting
            }).map { 
                case State(_, _, label, _) => q(label.toInt)
            }): Set[DFAState]
            
            def delta(s: DFAState, l: Letter): Set[DFAState] = {
                edges.foldLeft(Set(): Set[DFAState])(
                    (set, edge) => edge match {
                        case (from, label, to) =>
                            if (q(from.label.toInt).equals(s) && Symbol(label).equals(l))
                                set + q(to.label.toInt)
                            else
                                set
                    }
                )
            }
                
            Some((alphabet, dfaStates, dfaInitialState, delta _, dfaAccepting): NFA)
        } catch {
            case NonFatal(e) =>
                Dialog.showMessage(this, e.getMessage, "Error while constructing NFA", Dialog.Message.Error)
                None
        }
    }

    def dfa: Option[DFA] = {
        try {
            val dfaStates = states.map { 
                case State(_, _, label, _) => q(label.toInt)
            }: Set[DFAState]
            
            val dfaInitialState = (initialState match {
                case None => error("No initial state selected!")
                case Some(state) => q(state.label.toInt)
            }): DFAState
            
            val dfaAccepting = ((states.filter { 
                case State(_, _, _, isAccepting) => isAccepting
            }).map { 
                case State(_, _, label, _) => q(label.toInt)
            }): Set[DFAState]
            
            def delta(s: DFAState, l: Letter): DFAState = {
                val nextStateSet = edges.foldLeft(Set(): Set[DFAState])(
                    (set, edge) => edge match {
                        case (from, label, to) =>
                            if (q(from.label.toInt).equals(s) && Symbol(label).equals(l))
                                set + q(to.label.toInt)
                            else
                                set
                    }
                )
                
                if (nextStateSet isEmpty)
                    error("Transition function is not complete.")
                else
                    nextStateSet head
            }
                
            Some((alphabet, dfaStates, dfaInitialState, delta _, dfaAccepting): DFA)
        } catch {
            case NonFatal(e) =>
                Dialog.showMessage(this, e.getMessage, "Error while constructing DFA", Dialog.Message.Error)
                None
        }
    }
    
    private def alphabet: Set[Letter] =
        edges.foldLeft(Set(): Set[Letter])(
            (set,edge) => edge match {
                case (from, label, to) => set + Symbol(label)
            })
    
    private def getOuterOval(x: Int, y: Int) =
        new Ellipse2D.Float(
            x - (STATE_DIAMETER / 2),
            y - (STATE_DIAMETER / 2),
            STATE_DIAMETER,
            STATE_DIAMETER)

    private def getInnerOval(x: Int, y: Int) =
        new Ellipse2D.Float(
            x - (ACCPT_STATE_INNER_DIAMETER / 2),
            y - (ACCPT_STATE_INNER_DIAMETER / 2),
            ACCPT_STATE_INNER_DIAMETER,
            ACCPT_STATE_INNER_DIAMETER)

    private def getInnerstOval(x: Int, y: Int) =
        new Ellipse2D.Float(
            x - (ACCPT_STATE_INNERST_DIAMETER / 2),
            y - (ACCPT_STATE_INNERST_DIAMETER / 2),
            ACCPT_STATE_INNERST_DIAMETER,
            ACCPT_STATE_INNERST_DIAMETER)
    
    private def notInOuterBorder(point: Point) =
        point.x > BORDER_SIZE + (STATE_DIAMETER / 2) &&
        point.y > BORDER_SIZE + (STATE_DIAMETER / 2) &&
        point.x < size.width - (BORDER_SIZE + (STATE_DIAMETER / 2)) &&
        point.y < size.height - (BORDER_SIZE + (STATE_DIAMETER / 2))
        
    private def statesAtPoint(point: Point) = {
        val state = State(point.getX.toInt, point.getY.toInt, nextStateCounter.toString, false)
        ((states - state) filter {
            case State(otherX: Int, otherY: Int, _, _) =>
                getOuterOval(state.x, state.y) intersects getOuterOval(otherX, otherY).getBounds2D
        })
    }
        
    private def edgesAtPoint(point: Point) = {
        (edges filter {
            case (from,_,to) =>
                if (from equals to)
                    getOuterOval(from.x, from.y) contains point
                else {
                    val k1 = (point.x - from.x) / (to.x - from.x)
                    val k2 = (point.y - from.y) / (to.y - from.y)
                    
                    k1 == k2
                }
        })
    }
        
    private def replaceState(oldS: State, newS: State) = {
        states -= oldS
        states += newS
        
        initialState match {
            case None =>
            case Some(initialS) =>
                if (initialS equals oldS)
                    initialState = Some(newS)
        }
        
        selectedState match {
            case None =>
            case Some(selectedS) =>
                if (selectedS equals oldS)
                    selectedState = Some(newS)
        }
        
        edges = edges.map(
            edge => edge match {
                case (from,label,to) =>
                    val newFrom = if (from equals oldS) newS else from
                    val newTo = if (to equals oldS) newS else to
                    (newFrom,label,newTo)
            }
        )
        
        repaint
    }

    /**
     * Draws an arrow on the given Graphics2D context.
     *
     * This snippet is taken from http://www.bytemycode.com/snippets/snippet/82/.
     *
     * @param g The Graphics2D context to draw on
     * @param x The x location of the "tail" of the arrow
     * @param y The y location of the "tail" of the arrow
     * @param xx The x location of the "head" of the arrow
     * @param yy The y location of the "head" of the arrow
     */
    private def drawArrow(g: Graphics2D, x: Int, y: Int, xx: Int, yy: Int) {
        val arrowWidth = 10.0f
        val theta = 0.423f
        var xPoints = new Array[Int](3)
        var yPoints = new Array[Int](3)
        var vecLine = new Array[Float](2)
        var vecLeft = new Array[Float](2)
        var fLength = 0f
        var th = 0f
        var ta = 0f
        var baseX = 0f
        var baseY = 0f

        xPoints(0) = xx
        yPoints(0) = yy

        // build the line vector
        vecLine(0) = xPoints(0) - x
        vecLine(1) = yPoints(0) - y

        // build the arrow base vector - normal to the line
        vecLeft(0) = -vecLine(1)
        vecLeft(1) = vecLine(0)

        // setup length parameters
        fLength = Math.sqrt(vecLine(0) * vecLine(0) + vecLine(1) * vecLine(1)).toFloat
        th = arrowWidth / (2.0f * fLength);
        ta = arrowWidth / (2.0f * (Math.tan(theta) / 2.0f) * fLength).toFloat

        // find the base of the arrow
        baseX = (xPoints(0) - ta * vecLine(0))
        baseY = (yPoints(0) - ta * vecLine(1))

        // build the points on the sides of the arrow
        xPoints(1) = (baseX + th * vecLeft(0)).toInt
        yPoints(1) = (baseY + th * vecLeft(1)).toInt
        xPoints(2) = (baseX - th * vecLeft(0)).toInt
        yPoints(2) = (baseY - th * vecLeft(1)).toInt

        g.drawLine(x, y, baseX.toInt, baseY.toInt)
        g.fillPolygon(xPoints, yPoints, 3)
    }
}