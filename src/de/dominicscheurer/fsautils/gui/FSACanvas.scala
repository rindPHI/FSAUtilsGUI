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

class FSACanvas extends Panel {

    var states = Set[State]()
    var selectedState = None: Option[State]
    var initialState = None: Option[State]
    var edges = Set[(State, String, State)]()

    var nextStateCounter = 0
    var notPossibleStateHint = None: Option[State]

    val STATE_DIAMETER = 50
    val ACCPT_STATE_INNER_DIAMETER = 44
    val ACCPT_STATE_INNERST_DIAMETER = 20

    override def paintComponent(g: Graphics2D) {
        // Start by erasing this Canvas
        g.setColor(Color.WHITE)
        g.fillRect(0, 0, size.width, size.height)

        g.setFont(new Font("SansSerif", Font.PLAIN, 20))

        g setColor Color.BLACK
        for (edge <- edges) {
            edge match {
                case (from, label, to) => {
                    val r = STATE_DIAMETER / 2

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
                    } else {
                        // The edge is a loop
                        g.draw(getInnerOval(from.x + r, from.y - r))
                        drawArrow(g, from.x + r + 5, from.y - 4, from.x + r, from.y - 1)
                        
                        g.setColor(Color.WHITE)
                        g.fill(getOuterOval(from.x, from.y))
                        g.setColor(Color.BLACK)
                    }
                }
            }
        }

        for (state <- states) {
            g setColor Color.BLACK

            selectedState match {
                case None =>
                case Some(otherState) =>
                    if (state equals otherState)
                        g.setColor(Color.BLUE)
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
                                g.setColor(Color.GREEN)
                                g.fill(getInnerstOval(x, y))
                                g.setColor(Color.BLACK)
                            }
                    }

                    g.drawString(label, x - (5 * label.size), y + 7)

                }
            }
        }

        g.setColor(Color.RED)
        notPossibleStateHint match {
            case None        =>
            case Some(state) => notPossibleStateHint = None; g.draw(getOuterOval(state.x, state.y))
        }
    }

    def checkState(point: Point) {
        val state = State(point.getX.toInt, point.getY.toInt, nextStateCounter.toString, false)
        val intersectingStates = ((states - state) filter {
            case State(otherX: Int, otherY: Int, _, _) =>
                getOuterOval(state.x, state.y) intersects getOuterOval(otherX, otherY).getBounds2D
        })

        selectedState = None

        if (intersectingStates isEmpty) {

            states += state
            nextStateCounter += 1
            selectedState = Some(state)

        } else {

            notPossibleStateHint = Some(state)

        }

        repaint
    }

    def checkSelect(point: Point) {
        val state = State(point.getX.toInt, point.getY.toInt, nextStateCounter.toString, false)
        val intersectingStates = ((states - state) filter {
            case State(otherX: Int, otherY: Int, _, _) =>
                getOuterOval(state.x, state.y) intersects getOuterOval(otherX, otherY).getBounds2D
        })

        selectedState = None

        if (intersectingStates.size == 1) {

            selectedState = Some(intersectingStates head)

        }

        repaint
    }

    def checkEdge(point: Point) {
        val state = State(point.getX.toInt, point.getY.toInt, nextStateCounter.toString, false)
        val intersectingStates = ((states - state) filter {
            case State(otherX: Int, otherY: Int, _, _) =>
                getOuterOval(state.x, state.y) intersects getOuterOval(otherX, otherY).getBounds2D
        })

        selectedState match {
            case None =>
            case Some(otherState) =>
                if (intersectingStates.size == 1)
                    edges += ((otherState, "XXX", intersectingStates.head))
        }

        repaint
    }

    def checkAccepting = {
        selectedState match {
            case None =>
            case Some(State(x, y, label, accepting)) => {
                val oldState = State(x, y, label, accepting)
                val newState = State(x, y, label, !accepting)

                if (initialState equals Some(oldState))
                    initialState = Some(newState)

                states -= oldState
                states += newState
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