package de.dominicscheurer.fsautils.gui

import scala.io.Source._
import scala.swing._
import java.io.File
import java.io.PrintWriter

class XMLEditor(file: File)
extends SimpleSwingApplication {

    def top = new MainFrame {
        preferredSize = new Dimension(600, 400)
        title = file.getName
        
        val source = fromFile(file)
        val content = source.mkString
        source.close()
        
        val editorPane = new EditorPane("text/plain", content)
        
        val scrollPane = new ScrollPane()
        scrollPane.contents = editorPane
        contents = scrollPane
        
        menuBar = new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("Save XML File") {
                    Some(new PrintWriter(file)).foreach{p => p.write(editorPane.text); p.close}
                })
                contents += new MenuItem(Action("Close") {
                    close
                })
            }
        }
    }
    
}