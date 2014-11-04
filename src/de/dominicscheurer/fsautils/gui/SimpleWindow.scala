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

import scala.swing._
import scala.swing.BorderPanel.Position._
import event._
import java.awt.{ Color, Graphics2D }

object SimpleWindow extends SimpleSwingApplication {
    
    def top = new MainFrame {        
        title = "A Sample Scala Swing GUI"
    
        val canvas = new FSACanvas {
            preferredSize = new Dimension(500, 500)
        }
    
        contents = new BorderPanel {
            layout(canvas) = Center
        }
    
        size = new Dimension(700, 700)
    
        menuBar = new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("Exit") {
                    sys.exit(0)
                })
            }
        }
    
        // specify which Components produce events of interest
        listenTo(canvas.mouse.clicks)
    
        // react to events
        reactions += {
            case MouseClicked(_, point, _, clicks, _) =>
                if (clicks == 2)
                    canvas.addState(State(point.getX.toInt, point.getY.toInt, "Test", false, false))
        }
    }
}