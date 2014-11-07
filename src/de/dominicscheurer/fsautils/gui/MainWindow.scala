package de.dominicscheurer.fsautils.gui

import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Font
import scala.swing.event.MouseClicked
import javax.swing.ListSelectionModel
import java.io.File
import javax.swing.filechooser.FileFilter

object MainWindow extends SimpleSwingApplication with Observer[FSMCreationWindow] {
    
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
            contents += new Button("View") {
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
            contents += new Button("Edit") {
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
}