package de.dominicscheurer.fsautils.gui

import de.dominicscheurer.fsautils.DFA
import de.dominicscheurer.fsautils.Helpers._

object GraphvizBridge {
    def toDot(dfa: DFA) = {
        var sb = new StringBuilder
        sb ++= "digraph finite_state_machine {\n"
        sb ++= "  rankdir=LR;\n"
        sb ++= "  size=\"8,5\";\n\n"
        
        sb ++= "  node [shape = point ]; DUMMY\n"
        for (state <- dfa.accepting) {
            sb ++= "  node [ shape = doublecircle ]; "
            sb ++= replaceSpaces(state toString)
            sb ++= ";\n"
        }
        
        sb ++= "\n  node [ shape = circle ];\n"
        sb ++= "  DUMMY -> "
        sb ++= replaceSpaces(dfa.initialState.toString)
        sb ++= ";\n"
        
        cartesianProduct(dfa.states,dfa.alphabet).foreach({
                case (s,l) => {
                    sb ++= "  "
                    sb ++= replaceSpaces(s.toString)
                    sb ++= " -> "
                    sb ++= replaceSpaces(dfa.delta(s,l).toString)
                    sb ++= " [ label= \""
                    sb ++= l.name
                    sb ++= "\" ];\n"
                }
        })
        
        sb ++= "}"
        
        sb toString
    }
    
    private def replaceSpaces(s: String) =
        s.filter(_ != ' ')
}