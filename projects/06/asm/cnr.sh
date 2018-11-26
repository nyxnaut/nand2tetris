#!/bin/sh

# doesn't work yet because, well, I'm bad at bash =(

PROGRAM=$1
if [ ${PROGRAM: -4} != ".java" ]
then
	PROGRAM="$PROGRAM.java"
fi

CLASSPATH=
if [ -d "lib" ]
then
	for i in `ls lib/*.jar`
	do
		CLASSPATH=${CLASSPATH}:${i}
	done
fi

javac -classpath ".:${CLASSPATH}" -d "bin/" -g -encoding -Xlint:all -Xlint:overrides -Xmaxwarns 10 -Xmaxerrs 10 ${PROGRAM}

java -cp ".:${CLASSPATH};bin/" $2
