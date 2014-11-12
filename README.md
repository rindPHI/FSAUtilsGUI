FSAUtils GUI
============

GUI front end for the FSAUtils toolkit. Supports visual creation and manipulation
of Deterministic Finite Automata (DFA) and Nondeterministic Finite Automata (NFA).

**There exists a first 0.1-alpha release with an attached executable standalone jar file - check this out**
(https://github.com/rindPHI/FSAUtilsGUI/releases) **!**

Features supported so far
-------------------------

* Visual creation of DFA/NFA
* Visual edition of DFA/NFA
* Save and load from/to serialized XML
* Simple XML text editor
* Front-end for common operations supported by FSAUtils:
  * Acceptance / Equivalence check
  * Union, Intersection, Star, ...
  * Minimization of DFA
  * Determinization of NFA
  * Extraction of Regular Expressions
  
Note that the current release is a non-production one, so you might experience glitches
for instance in the visual editor. If you have problems with the editor, you still have
the option of directly editing the XML file, so keep this in mind ;)

Get Started
-----------

**Prerequisites:** You need to have Scala and the JVM installed. FSAUtils GUI
has been tested with Scala 2.11.2 and Java 1.7. Furthermore, the environment
variable `$SCALA_HOME` has to be correctly set to the path where Scala resides.

The following steps should work for a Linux system.

1. Download and build FSAUtils (https://github.com/rindPHI/FSAUtils).

2. Download the archive:
   
   ```bash
   wget https://github.com/rindPHI/FSAUtilsGUI/archive/master.zip -O FSAUtilsGUI-master.zip
   ```
   
3. Unzip it:
   
   ```bash
   unzip FSAUtilsGUI-master.zip
   ```
   
4. Build it:
   
   ```bash
   cd FSAUtilsGUI-master
   ant
   ```
   
   As the result, you find an executable jar file "FSAUtils_GUI.jar" in the directory `lib/`.
   
5. ...and run it:
   
   ```bash
   java -jar lib/FSAUtils_GUI.jar
   ```

License
-------

Copyright 2014 Dominic Scheurer
    
This file is part of FSAUtilsGUI.
     
FSAUtilsGUI is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
     
FSAUtilsGUI is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
     
You should have received a copy of the GNU General Public License
along with FSAUtilsGUI.  If not, see <http://www.gnu.org/licenses/>.
