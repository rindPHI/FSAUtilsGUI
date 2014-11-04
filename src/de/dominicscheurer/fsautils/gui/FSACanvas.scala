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

class FSACanvas extends Panel {
    
  var centerColor = Color.yellow
  var states = List[State]()
  
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
                g.drawOval(
                        x - (STATE_DIAMETER / 2),
                        y - (STATE_DIAMETER / 2),
                        STATE_DIAMETER,
                        STATE_DIAMETER)
                if (isAccepting)
                    g.drawOval(
                            x - (ACCPT_STATE_INNER_DIAMETER / 2),
                            y - (ACCPT_STATE_INNER_DIAMETER / 2),
                            ACCPT_STATE_INNER_DIAMETER,
                            ACCPT_STATE_INNER_DIAMETER)
            }
        }
    }
  }
  
  def addState(state: State) {
      states = states :+ state
      repaint
  }
  
}