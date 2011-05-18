# Makefile

all: compile javadoc

compile:
	(rm -r classes/eduni)
	(javac -d classes source/eduni/simjava/*.java)
	(javac -d classes source/eduni/simjava/distributions/*.java)
	(javac -d classes source/eduni/simanim/*.java)
	(javac -d classes source/eduni/simdiag/*.java)
	(rm -r docs; mkdir docs)

javadoc:
	(javadoc @options @classfiles)
