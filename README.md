FSAUtils GUI
============

GUI front end for the FSAUtils toolkit.

Features supported so far
-------------------------

* (...)

Get Started
-----------

**Prerequisites:** You need to have Scala and the JVM installed. FSAUtils GUI
has been tested with Scala 2.11 and Java 1.7. Furthermore, the environment
variable `$SCALA_HOME` has to be correctly set to the path where Scala resides.

The following steps should work for a Linux system.

1. Download and build FSAUtils (https://github.com/rindPHI/FSAUtilsGUI).

2. Download the archive:
   
   ```bash
   wget https://github.com/rindPHI/FSAUtilsGUI/archive/master.zip
   ```
   
3. Unzip it:
   
   ```bash
   unzip master.zip
   ```
   
4. Build it:
   
   ```bash
   cd FSAUtilsGUI-master
   ant
   ```
   
   As the result, you find a file "FSAUtils_GUI.jar" in the directory `lib/`
   which you need to add to the classpath of scalac and scala in order
   to compile / run your objects that make use of FSAUtils.
   
5. ...and run it:
   
   ```bash
   scala -classpath ".:/path/to/FSAUtils.jar:/path/to/FSAUtils_GUI.jar" de.dominicscheurer.fsautils.gui.SimpleWindow
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
