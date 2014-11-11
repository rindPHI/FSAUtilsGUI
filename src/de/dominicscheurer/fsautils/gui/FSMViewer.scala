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

import de.dominicscheurer.fsautils.DFA
import de.dominicscheurer.fsautils.NFA
import de.dominicscheurer.fsautils.Types._

import scala.io.Source._
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.sys.process._
import java.io.File
import java.io.PrintWriter
import scala.xml.XML
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.filechooser.FileFilter
import event._

class FSMViewer(var file: File)
extends SimpleSwingApplication {

    type MutableSet[A] = scala.collection.mutable.Set[A]
    def MutableSet[A](): MutableSet[A] = scala.collection.mutable.Set[A]()
    
    val SCALE_FACTOR = 150
    
    var shiftHold = false
    val canvas = new FSMCanvas {
        preferredSize = new Dimension(500, 500)
        focusable = true
    }
    
    def top = new MainFrame {
        preferredSize = new Dimension(600, 400)
        title = file.getName
        
        import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
        peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        override def closeOperation() { close }
        
        val source = fromFile(file)
        val content = source.mkString
        source.close()
        
        val dotCode =
            if (file.getName.endsWith("dfa.xml"))
                GraphvizBridge.toDot(DFA.fromXml(XML.loadString(content)))
            else
                GraphvizBridge.toDot(NFA.fromXml(XML.loadString(content)))
        
        val tmpInputFile = java.io.File.createTempFile("FSAUtils", ".dot")
        tmpInputFile.deleteOnExit()
        Some(new PrintWriter(tmpInputFile)).foreach{p => p.write(dotCode); p.close}  
        
        val tmpOutputFile = java.io.File.createTempFile("FSAUtils", ".plain")
        tmpOutputFile.deleteOnExit()
        
        menuBar = new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("Save") {
                    canvas.fsm match {
                        case None =>
                        case Some(fsm) => {
                            Some(new PrintWriter(file)).foreach{p => p.write(fsm.toPrettyXml); p.close}
                        }
                    }
                })
                contents += new MenuItem(Action("Save Copy As") {
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
                            }
                        }
                    }
                })
                contents += new MenuItem(Action("Close") {
                    close
                })
            }
        }
        
        if (Seq("dot", "-V").! == 0) {
        
            if (Seq("dot", "-Tplain", "-o", tmpOutputFile.toString, tmpInputFile.toString).! == 0) {
                
                val plainOutput = fromFile(tmpOutputFile)
                
                var states = MutableSet[State]()
                var edges = MutableSet[(State, String, State)]()
                var initialState = None: Option[State]
                
                plainOutput.getLines().foreach {
                    line => parseLine(line, states, edges, initialState) match {
                        case None =>
                        case Some(state) => initialState = Some(state)
                    }
                }
                
                plainOutput.close()
                
                canvas.states = Set() ++ states
                canvas.edges = Set() ++ edges
                canvas.initialState = initialState
                
                val scrollPane = new ScrollPane()
                scrollPane.contents = new BorderPanel {
                    layout(canvas) = Center
                }
                contents = scrollPane
                
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
                    case KeyReleased(_, Key.Delete, _, _) =>
                        canvas checkDelete
                    case KeyReleased(_, Key.F2, _, _) =>
                        canvas checkRename
                    case KeyReleased(_, Key.Shift, _, _) =>
                        shiftHold = false
                    case KeyPressed(_, Key.Shift, _, _) =>
                        shiftHold = true
                }
                
            } else {
                //TODO: Error message
            }
            
        } else {
            //TODO: Message: Not dot found
        }
    }
    
    /**
     * @return The initial state of the FSM or None. States and edges
     *   are directly manipulated.
     */
    private def parseLine(
            line: String,
            states: MutableSet[State],
            edges: MutableSet[(State, String, State)],
            initialState: Option[State]): Option[State] = {
        val tokens = line.split(' ')
        if ((tokens(0) equals "node") && !(tokens(1) equals "DUMMY"))
            states += State(
                    Math.round((tokens(2).toFloat * SCALE_FACTOR)).toInt + canvas.BORDER_SIZE,
                    Math.round((tokens(3).toFloat * SCALE_FACTOR)).toInt + canvas.BORDER_SIZE,
                    tokens(1),
                    tokens(8) equals "doublecircle")
        else if ((tokens(0) equals "edge") && !(tokens(1) equals "DUMMY"))
            edges +=
                (
                    ((states.find { case State(_,_,l,_) => (l equals tokens(1)) }).get,
                    tokens(tokens.length - 5),
                    (states.find { case State(_,_,l,_) => (l equals tokens(2)) }).get)
                )
        else if ((tokens(0) equals "edge") && (tokens(1) equals "DUMMY"))
            return Some((states.find { case State(_,_,l,_) => (l equals tokens(2)) }).get)
        
        None
    }
}