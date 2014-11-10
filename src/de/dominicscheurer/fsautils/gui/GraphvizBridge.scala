package de.dominicscheurer.fsautils.gui

import de.dominicscheurer.fsautils.DFA
import de.dominicscheurer.fsautils.Helpers._
import de.dominicscheurer.fsautils.NFA
import de.dominicscheurer.fsautils.Types._

object GraphvizBridge {
    type FSMState = de.dominicscheurer.fsautils.Types.State

    def toDot(dfa: DFA) = {
        var sb = new StringBuilder
        toDotFSMNodes(sb, dfa.accepting, dfa.initialState)
        
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
    
    def toDot(nfa: NFA) = {
        var sb = new StringBuilder
        toDotFSMNodes(sb, nfa.accepting, nfa.initialState)
        
        cartesianProduct(nfa.states,nfa.alphabet).foreach({
                case (s,l) => 
                    nfa.delta(s,l).foreach(
                        to => {
                            sb ++= "  "
                            sb ++= replaceSpaces(s.toString)
                            sb ++= " -> "
                            sb ++= replaceSpaces(to.toString)
                            sb ++= " [ label= \""
                            sb ++= l.name
                            sb ++= "\" ];\n"
                    })
        })
        
        sb ++= "}"
        
        sb toString
    }
    
    private def toDotFSMNodes(sb: StringBuilder, accepting: Set[FSMState], initialState: FSMState) = {
        sb ++= "digraph finite_state_machine {\n"
        sb ++= "  rankdir=LR;\n"
        sb ++= "  size=\"8,5\";\n\n"
        
        sb ++= "  node [shape = point ]; DUMMY\n"
        for (state <- accepting) {
            sb ++= "  node [ shape = doublecircle ]; "
            sb ++= replaceSpaces(state toString)
            sb ++= ";\n"
        }
        
        sb ++= "\n  node [ shape = circle ];\n"
        sb ++= "  DUMMY -> "
        sb ++= replaceSpaces(initialState.toString)
        sb ++= ";\n"
    }
    
    private def replaceSpaces(s: String) =
        s.filter(_ != ' ')
}