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
import scala.swing.event._
import javax.swing._
import javax.swing.text._
import java.awt.event._

/**
 * A text component that allows for multi-line text input and display.
 *
 * @see javax.swing.JEditorPane
 */
class EditorPane(contentType0: String, text0: String) extends TextComponent {
    override lazy val peer: JEditorPane = new JEditorPane(contentType0, text0) with SuperMixin
    def this() = this("text/plain", "")
    
    def contentType: String = peer.getContentType
    def contentType_=(t: String) = peer.setContentType(t)
    
    def editorKit: EditorKit = peer.getEditorKit
    def editorKit_=(k: EditorKit) = peer.setEditorKit(k)
}