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
import de.dominicscheurer.fsautils.FSM
import de.dominicscheurer.fsautils.RegularExpressions._

import scala.io.Source._
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.event.MouseClicked
import scala.swing.event.SelectionChanged
import scala.sys.process._
import scala.xml.XML

import java.awt.Font
import java.io.File
import java.io.PrintWriter
import javax.swing.filechooser.FileFilter
import javax.swing.ListSelectionModel

object MainWindow extends SimpleSwingApplication with Observer[FSMCreationWindow] {
    
    val STD_IMAGE_EXPORT_TYPE = "png"
    
    var loadedAutomata = Map(): Map[String, File]// ListView of FSMs
    val listView = new ListView(loadedAutomata.keys.toSeq) {
        font = new Font("Sans Serif", Font.PLAIN, 20)
    }
    
    def top = new MainFrame {
            
        // Title
        title = "FSM Utilities"
        val titleLabel = new Label("FSM Manager") {
            font = new Font("Sans Serif", Font.PLAIN, 40)
        }
        
        preferredSize = new Dimension(600, 400)
        
        listView.peer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        val scrollPane = new ScrollPane()
        scrollPane.contents = listView
        
        menuBar = new MenuBar
        
        // Menu Items
        val fsmMenu = new Menu("FSM Actions") {
            contents += new MenuItem(Action("Export as Image") {
                val fsm = getFSM(selectedFile)
                
                val chooser = new FileChooser(new File("."))
                
                List(
                    fileFilterFor("bmp", "Windows Bitmap format (BMP)"),
                    fileFilterFor("gif", "Graphics Interchange Format (GIF)"),
                    fileFilterFor("jpg", "Joint Photographic Experts Group format (JPG)"),
                    fileFilterFor("pdf", "Portable Document Format (PDF)"),
                    fileFilterFor("png", "Portable Network Graphics format (PNG)"),
                    fileFilterFor("ps",  "PostScript (PS)"),
                    fileFilterFor("svg", "Scalable Vector Graphics (SVG)"),
                    fileFilterFor("vml", "Vector Markup Language (VML)")).foreach {
                        filter => chooser.peer.addChoosableFileFilter(filter)
                    }
                chooser.fileFilter = fileFilterFor("png", "Portable Network Graphics format (PNG)")
                
                chooser.title = "Export " + (if (fsm.isDFA) "DFA" else "NFA")
                chooser.peer.setAcceptAllFileFilterUsed(false)
                
                val result = chooser.showOpenDialog(this)
                if (result == FileChooser.Result.Approve) {
                    
                    val selFile =  
                        if (chooser.selectedFile.getName.split('.').size > 1)
                            chooser.selectedFile
                        else
                            new File(chooser.selectedFile.toString + "." + STD_IMAGE_EXPORT_TYPE)
                    val ending = 
                        if (selFile.getName.split('.').size > 1)
                            selFile.getName.split('.').last
                        else
                            STD_IMAGE_EXPORT_TYPE
                    
                    // Load selected automaton file
                    val source = fromFile(selectedFile)
                    val content = source.mkString
                    source.close()
                    
                    // Generate dot code
                    val dotCode =
                        if (selectedFile.getName.endsWith("dfa.xml"))
                            GraphvizBridge.toDot(DFA.fromXml(XML.loadString(content)))
                        else
                            GraphvizBridge.toDot(NFA.fromXml(XML.loadString(content)))
                    
                    // Write dot code
                    val tmpInputFile = java.io.File.createTempFile("FSAUtils", "." + ending)
                    tmpInputFile.deleteOnExit()
                    Some(new PrintWriter(tmpInputFile)).foreach{p => p.write(dotCode); p.close}
                    
                    // Transform with Graphviz
                    if ((Seq("dot", "-V").! == 0) &&
                        (Seq("dot", "-T" + ending, "-o", selFile.toString, tmpInputFile.toString).! == 0)) {
                        Dialog.showMessage(
                                this,
                                "Image has been saved to " + selFile.toString,
                                "Output images was successfully generated")
                    } else {
                        Dialog.showMessage(
                                this,
                                "Either the dot command is not in your path (is Graphviz installed?), "+
                                  "or there were other problems while generating your output image " +
                                  "(make sure your chose a correct image extension and path).",
                                "Error: Output images was not successfully generated")
                    }
                }
            })
            contents += new Separator
            contents += new MenuItem(Action("Check word acceptance") {
                Dialog.showInput(
                    this,
                    "Enter the word to check.",
                    title = "Word acceptance check",
                    initial = "") match {
                        case None =>
                        case Some(word) =>
                            val fsm: FSM = getFSM(selectedFile)
                            val result = fsm.accepts(word)
                            
                            Dialog.showMessage(
                                this,
                                "The automaton " + selectedFile.getName +
                                (if (result) " DOES" else " DOES NOT") +
                                " accept the word '" +
                                word + "'.",
                                "Word acceptance check")
                    }
            })
            contents += new MenuItem(Action("({}) Check for Emptyness") {
                val fsm: FSM = getFSM(selectedFile)
                val result = fsm.isEmpty
                
                Dialog.showMessage(
                    this,
                    "The automaton " + selectedFile.getName +
                    (if (result) " IS" else " IS NOT") + " empty.",
                    "Check for Emptyness")
            })
            contents += new MenuItem(Action("(=) Check Equivalence") {
                getBinaryFSMOpInput(this) match {
                    case None =>
                    case Some(fileName) =>
                        val (firstFsm,secondFsm) =
                            adjustAlphabets(
                                    getFSM(selectedFile),
                                    getFSM(loadedAutomata(fileName)))
                        
                        val result = (firstFsm == secondFsm)
                        
                        Dialog.showMessage(
                            this,
                            "The automaton " + selectedFile.getName +
                            (if (result) " EQUALS" else " DOES NOT EQUAL") +
                            " the automaton " + fileName + ".",
                            "Check for Equivalence")
                }
            })
            contents += new Separator
            contents += new MenuItem(Action("(RE) Get Regular Rxpression") {
                val fsm = getFSM(selectedFile)
                val re: RE = fsm.asDFA match {
                    case None => fsm.asNFA.get.toRegExp
                    case Some(dfa) => dfa.minimize.toRegExp
                }
                
                Dialog.showInput(
                        this,
                        "This is the generated Regular Expression",
                        "Regular Expression Conversion",
                        initial = re.clean.cleanString)
            })
            contents += new Separator
            contents += new MenuItem(Action("(+) Concatenation") {
                getBinaryFSMOpInput(this) match {
                    case None =>
                    case Some(fileName) =>
                        val (firstFsm,secondFsm) =
                            adjustAlphabets(
                                    getFSM(selectedFile),
                                    getFSM(loadedAutomata(fileName)))
                        
                        val result = (firstFsm ++ secondFsm)
                        showFSM(result)
                }
            })
            contents += new MenuItem(Action("(&) Intersection") {
                getBinaryFSMOpInput(this) match {
                    case None =>
                    case Some(fileName) =>
                        val (firstFsm,secondFsm) =
                            adjustAlphabets(
                                    getFSM(selectedFile),
                                    getFSM(loadedAutomata(fileName)))
                        
                        val result = (firstFsm & secondFsm)
                        showFSM(result)
                }
            })
            contents += new MenuItem(Action("(|) Union") {
                getBinaryFSMOpInput(this) match {
                    case None =>
                    case Some(fileName) =>
                        val (firstFsm,secondFsm) =
                            adjustAlphabets(
                                    getFSM(selectedFile),
                                    getFSM(loadedAutomata(fileName)))
                        
                        val result = (firstFsm | secondFsm)
                        showFSM(result)
                }
            })
            contents += new MenuItem(Action("(\\) Difference") {
                getBinaryFSMOpInput(this) match {
                    case None =>
                    case Some(fileName) =>
                        val (firstFsm,secondFsm) =
                            adjustAlphabets(
                                    getFSM(selectedFile),
                                    getFSM(loadedAutomata(fileName)))
                        
                        val result = (firstFsm \ secondFsm)
                        showFSM(result)
                }
            })
            contents += new MenuItem(Action("(*) Star") {
                val fsm = getFSM(selectedFile)
                showFSM(fsm*)
            })
        }
        
        val dfaMenu = new Menu("DFA Actions") {
            contents += new MenuItem(Action("Minimize") {
                val dfa = getFSM(selectedFile).asDFA.get
                showFSM(dfa minimize)
            })
        }
        
        val nfaMenu = new Menu("NFA Actions") {
            contents += new MenuItem(Action("Determinize") {
                val nfa = getFSM(selectedFile).asNFA.get
                showFSM(nfa toDFA)
            })
        }
        
        listView.listenTo(listView.selection)
        listView.reactions += {
            case SelectionChanged(_) => {
                menuBar.contents.clear
                
                if (listView.selection.items.size == 1) {
                    menuBar.contents += fsmMenu
                    
                    if (listView.selection.items(0).endsWith("dfa.xml"))
                        menuBar.contents += dfaMenu
                    else if (listView.selection.items(0).endsWith("nfa.xml"))
                        menuBar.contents += nfaMenu
                }
                
                pack
                repaint
            }
        }
        
        // FSM List Controls
        val buttonSize = new Dimension(130,50)
        val fsmCtrls = new BoxPanel(Orientation.Vertical) {
            contents += new Button("Create") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize
                
                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) => {
                            val fsmCreationWindow = new FSMCreationWindow()
                            fsmCreationWindow.addObserver(MainWindow)
                            fsmCreationWindow.startup(Array())
                        }
                }
            }
            contents += new Button("Load") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize
                
                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) =>
                        val chooser = new FileChooser(new File("."))
                        chooser.title = "Load existing automaton"
                        chooser.fileFilter = new FileFilter() {
                            def accept(f: File) =
                                f.getName.endsWith("dfa.xml") || f.getName.endsWith("nfa.xml") || f.isDirectory()
                            def getDescription = "DFA or NFA Files"
                        }
                        chooser.peer.setAcceptAllFileFilterUsed(false)
                        
                        val result = chooser.showOpenDialog(null)
                        if (result == FileChooser.Result.Approve) {
                            loadedAutomata += (chooser.selectedFile.getName -> chooser.selectedFile)
                            reloadListView
                        }
                }
            }
            contents += new Button("Remove") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize
                 
                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) =>
                        if (listView.selection.items.size == 1)
                            listView.listData = (listView.listData.toSet - listView.selection.items(0)).toSeq
                }
            }
            contents += new Button("Edit") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize

                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) =>
                        if (listView.selection.items.size == 1) {
                            val fsmViewer = new FSMViewer(loadedAutomata(listView.selection.items(0)))
                            fsmViewer.startup(Array())
                        }
                }
            }
            contents += new Button("Edit XML") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize

                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) =>
                        if (listView.selection.items.size == 1) {
                            val xmlEditor = new XMLEditor(loadedAutomata(listView.selection.items(0)))
                            xmlEditor.startup(Array())
                        }
                }
            }
        }
        
        contents = new BorderPanel {
            layout(titleLabel) = North
            layout(scrollPane) = Center
            layout(fsmCtrls) = East
        }
    }

    def receiveUpdate(subject: FSMCreationWindow): Unit = {
        val file = subject.file
        if (file != null) {
            loadedAutomata += (file.getName -> file)
            reloadListView
        }
    }
    
    private def reloadListView = {
        listView.listData = loadedAutomata.keys.toSeq
    }
    
    private def selectedFile =
        loadedAutomata(listView.selection.items(0))
    
    private def getBinaryFSMOpInput(parent: Component) =
        Dialog.showInput(
            parent,
            message = "Choose another automaton",
            title = "Binary operation on automata",
            entries = loadedAutomata.keys.toSeq,
            initial = loadedAutomata.keys.toSeq.head)
        
    private def adjustAlphabets(fsm1: FSM, fsm2: FSM): (FSM,FSM) = {
        val fsm1A = fsm1.adjustAlphabet(fsm2)
        val fsm2A = fsm2.adjustAlphabet(fsm1)
        
        (fsm1A -> fsm2A)
    }
            
    private def getFSM(file: File): FSM = {
        val source = fromFile(file)
        val content = source.mkString
        source.close()
        
        if (file.getName.endsWith("dfa.xml"))
            DFA.fromXml(XML.loadString(content))
        else
            NFA.fromXml(XML.loadString(content))
    }
    
    private def showFSM(fsm: FSM) = {
        val xmlCode = fsm.toPrettyXml.toString
        
        val tmpFile = java.io.File.createTempFile("FSAUtils", if (fsm.isDFA) ".dfa.xml" else ".nfa.xml")
        tmpFile.deleteOnExit()
        Some(new PrintWriter(tmpFile)).foreach{p => p.write(xmlCode); p.close}
        
        val fsmViewer = new FSMViewer(tmpFile)
        fsmViewer.startup(Array())
    }
    
    private def fileFilterFor(ending: String, description: String) =
        new FileFilter() {
            def accept(f: File) =
                f.getName.toLowerCase.endsWith(ending) ||
                f.isDirectory()
            def getDescription = description
        }
}