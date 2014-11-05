package de.dominicscheurer.fsautils.gui

import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Font
import scala.swing.event.MouseClicked
import javax.swing.ListSelectionModel
import java.io.File

object MainWindow extends SimpleSwingApplication {
    
    def top = new MainFrame {
        
        // Title
        title = "FSM Utilities"
        val titleLabel = new Label("FSM Manager") {
            font = new Font("Sans Serif", Font.PLAIN, 40)
        }
        
        preferredSize = new Dimension(600, 400)
        
        // ListView of FSMs
        var loadedAutomata = Map(): Map[String, File]
        val listView = new ListView(loadedAutomata.keys.toSeq)
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
            }
            contents += new Button("Load") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize
                
                listenTo(mouse.clicks)
                reactions += {
                    case MouseClicked(_, _, _, _, _) =>
                        val chooser = new FileChooser(new File("."))
                        chooser.title = ""
                        val result = chooser.showOpenDialog(null)
                        if (result == FileChooser.Result.Approve) {
                            loadedAutomata += (chooser.selectedFile.getName -> chooser.selectedFile)
                            listView.listData = loadedAutomata.keys.toSeq
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
            }
            contents += new Button("Edit") {
                minimumSize = buttonSize
                maximumSize = buttonSize
                preferredSize = buttonSize
            }
        }
        
        contents = new BorderPanel {
            layout(titleLabel) = North
            layout(scrollPane) = Center
            layout(fsmCtrls) = East
        }
    }
    
}