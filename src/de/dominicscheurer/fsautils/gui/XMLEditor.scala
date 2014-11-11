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

import scala.io.Source._
import scala.swing._
import java.io.File
import java.io.PrintWriter

class XMLEditor(file: File)
extends SimpleSwingApplication {

    def top = new MainFrame {
        preferredSize = new Dimension(600, 400)
        title = file.getName
        
        import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
        peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        override def closeOperation() { close }
        
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