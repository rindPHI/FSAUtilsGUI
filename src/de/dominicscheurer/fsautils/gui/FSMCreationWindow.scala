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
import java.io.File
import java.io.PrintWriter
import javax.swing.filechooser.FileFilter

class FSMCreationWindow extends SimpleSwingApplication with Subject[FSMCreationWindow] {
    
    var shiftHold = false
    var file: File = null
    val canvas = new FSACanvas {
        preferredSize = new Dimension(500, 500)
        focusable = true
    }
    
    def top = new MainFrame {
        title = "A Sample Scala Swing GUI"
    
        contents = new BorderPanel {
            layout(canvas) = Center
        }
    
        size = new Dimension(700, 700)
    
        menuBar = new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("Save File") {
                    canvas.fsm match {
                        case None =>
                        case Some(fsm) => {
                            val ending = if (fsm.isDFA) ".dfa.xml" else ".nfa.xml"
                            val chooser = new FileChooser(new File("."))
                            chooser.fileFilter = new FileFilter() {
                                def accept(f: File) =
                                    f.getName.endsWith(ending) || f.isDirectory()
                                def getDescription =
                                    if (fsm.isDFA) "DFA Files" else "NFA Files"
                            }
                            chooser.title = "Save " + (if (fsm.isDFA) "DFA" else "NFA")
                            chooser.peer.setAcceptAllFileFilterUsed(false)
                            
                            val result = chooser.showOpenDialog(this)
                            if (result == FileChooser.Result.Approve) {
                                file =
                                    if (chooser.selectedFile.getName.endsWith(ending))
                                        chooser.selectedFile
                                    else
                                        new File(chooser.selectedFile.getParent, chooser.selectedFile.getName + ending)
                                Some(new PrintWriter(file)).foreach{p => p.write(fsm.toPrettyXml); p.close}
                                notifyObservers
                            }
                        }
                    }
                })
                contents += new MenuItem(Action("Close") {
                    close
                })
            }
        }
    
        // specify which Components produce events of interest
        listenTo(canvas.mouse.clicks)
        listenTo(canvas.keys)
    
        // react to events
        reactions += {
            case MouseClicked(_, point, _, clicks, _) =>
                if (clicks == 1 && shiftHold) {
                    shiftHold = false // Hack due to input prompt, otherwise
                                      // selection does not get forgotten
                    canvas checkEdge point
                }
                else if (clicks == 1 && !shiftHold)
                    canvas checkSelect point
                else if (clicks == 2)
                    canvas checkState point
            case KeyTyped(_, 'a', _, _) =>
                canvas checkAccepting
            case KeyTyped(_, 'i', _, _) =>
                canvas checkInitial
            case KeyReleased(_, Key.Shift, _, _) =>
                shiftHold = false
            case KeyPressed(_, Key.Shift, _, _) =>
                shiftHold = true
        }
    }
}