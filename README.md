# Credibility Graphs

## About

This project implements operators of the multi-source belief revision as presented in:
_Tamargo, Luciano H., et al. On the revision of informant credibility orders. Artificial Intelligence, 2014._
 

## Installation

**Prerequisites:**

* Java 8 or higher. 
* [Git](https://git-scm.com/download/win)

**Installation steps:**

1. Install [Graphviz.](https://graphviz.gitlab.io/_pages/Download/Download_windows.html)

2. Add Graphviz to your system `PATH` variable. You have to modify the variable to include
   Graphviz's binaries. [This page](https://www.computerhope.com/issues/ch000549.htm) shows
   how to modify the `PATH` variable in several Windows versions.

   You modify the variable by appending the location of Graphviz's bin directory. If the
   directory was located at `C:\Program Files\Graphviz\bin`, you'd append: 
   `;C:\Program Files\Graphviz\bin`  -- mind the semicolon at the beginning.

3. Install [IntelliJ IDEA Community Edition.](https://www.jetbrains.com/idea/download)

4. To clone the project, start IDEA, select `Checkout from Version Control`, then select `Git`
   (not `GitHub`), and in the `Git Repository URL` write:
   `https://github.com/djelenc/credibility-graphs.git`

   Open the project when prompted. The examples are provided in
   `src/main/java/credibilitypgraphs/App.java`.