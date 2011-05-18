# Makefile

all: compile javadoc

compile:
	(rm -r classes/eduni)
	(javac -d ../classes eduni/simjava/*.java)
	(javac -d ../classes eduni/simjava/distributions/*.java)
	(javac -d ../classes eduni/simanim/*.java)
	(javac -d ../classes eduni/simdiag/*.java)
	(rm -r docs; mkdir docs)

javadoc:
	(javadoc @options @classfiles)
