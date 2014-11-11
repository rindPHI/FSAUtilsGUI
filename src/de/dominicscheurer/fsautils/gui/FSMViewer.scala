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
import scala.io.Source._
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.sys.process._
import java.io.File
import java.io.PrintWriter
import scala.xml.XML
import javax.swing.ImageIcon
import javax.swing.JLabel

class FSMViewer(file: File)
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
        
        val dotCode =
            if (file.getName.endsWith("dfa.xml"))
                GraphvizBridge.toDot(DFA.fromXml(XML.loadString(content)))
            else
                GraphvizBridge.toDot(NFA.fromXml(XML.loadString(content)))
        
        val tmpInputFile = java.io.File.createTempFile("FSAUtils", ".dot")
        tmpInputFile.deleteOnExit()
        Some(new PrintWriter(tmpInputFile)).foreach{p => p.write(dotCode); p.close}
        
        
        val tmpOutputFile = java.io.File.createTempFile("FSAUtils", ".png")
        tmpOutputFile.deleteOnExit()
        
        if (Seq("dot", "-V").! == 0) {
        
            if (Seq("dot", "-Tpng", "-o", tmpOutputFile.toString, tmpInputFile.toString).! == 0) {
                
                val editorPane = new Label
                editorPane.icon = new ImageIcon(tmpOutputFile.toString)
                
                val panel = new BorderPanel {
                    layout(editorPane) = Center
                }
                
                val scrollPane = new ScrollPane()
                scrollPane.contents = panel
                contents = scrollPane
                
            } else {
                //TODO: Error message
            }
            
        } else {
            //TODO: Message: Not dot found
        }
        
        menuBar = new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("Close") {
                    close
                })
            }
        }
    }
    
}