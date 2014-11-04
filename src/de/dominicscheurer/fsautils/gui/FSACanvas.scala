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
import java.awt.{ Graphics2D, Color }
import java.awt.geom.Ellipse2D

class FSACanvas extends Panel {

    var states = List[State]()
    var notPossibleStateHint = None: Option[State]

    val STATE_DIAMETER = 50
    val ACCPT_STATE_INNER_DIAMETER = 44

    override def paintComponent(g: Graphics2D) {
        // Start by erasing this Canvas
        g.setColor(Color.WHITE)
        g.fillRect(0, 0, size.width, size.height)

        g.setColor(Color.BLACK)
        for (state <- states) {
            state match {
                case State(x: Int, y: Int, label: String, isStart: Boolean, isAccepting: Boolean) => {

                    g.draw(getOuterOval(x, y))

                    if (isAccepting)
                        g.draw(getInnerOval(x, y))

                }
            }
        }
        
        g.setColor(Color.RED)
        notPossibleStateHint match {
            case None =>
            case Some(state) => notPossibleStateHint = None; g.draw(getOuterOval(state.x, state.y))
        }
    }

    def addState(state: State) {
        if (((states.toSet - state) filter {
            case State(otherX: Int, otherY: Int, _, _, _) =>
                getOuterOval(state.x, state.y) intersects getOuterOval(otherX, otherY).getBounds2D
        }).isEmpty) {
            
            states = states :+ state
            
        } else {
            
            notPossibleStateHint = Some(state)
            
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
}