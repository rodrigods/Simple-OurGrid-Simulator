###############################
#    NOTES FOR SIMJAVA 2.0    #
###############################

1. Directories and files:

- classes: contains the class files for SimJava 2.0 and the SJGV.
- source: contains the source code for SimJava 2.0 and the SJGV.
- jars: contains the jar files used for compiling and running simulations.
- doc: the Javadoc API specification for SimJava 2.0.
- tutorial: the SimJava 2.0 tutorial.
- source/makefile: the makefile used to compile the SimJava source and generate the API docs.
- source/options & source/classfiles: files containing parameters for the API doc generation.

2. To generate the API docs manually:

  Within the source directory type:

  javadoc @options @classfiles

  This will generate the full API docs in a directory simjava2/docs. The documentation in directory
  doc contains a reduced version that contains only the information required for SimJava users.

3. To compile the source code manually:

  - For SimJava 2.0:

    Within the source directory:

    mkdir class;
    javac -d class eduni/simjava/*.java;
    javac -d class eduni/simjava/distributions/*.java;
    javac -d class eduni/simanim/*.java;
    javac -d class eduni/simdiag/*.java;

  - For the SJGV:

    Within the source/SJGV directory:

    javac *.java

4. To build the jar files:

   jar -cf <name>.jar *

   For example:

   jar -cf simjava.jar classes/eduni

5. To compile and run simulations add simjava.jar to the classpath. For example:

   javac -classpath jars/simjava.jar Simulation.java
   java -classpath jars/simjava.jar:. Simulation

   Alternatively, the "classes" directory can be added to the classpath. For example:

   javac -classpath classes Simulation.java
   java -classpath classes:. Simulation

6. To run the SJGV:

   java -classpath jars/SJGV.jar SJGV [<name>.sjg]

   or

   java -classpath classes/SJGV SJGV [<name>.sjg]

