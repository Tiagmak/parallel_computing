#! /bin/bash
. $(dirname $0)/env.sh
mkdir -p classes
/opt/java/jdk1.8.0_112/bin/javac -d classes -cp $CLASSPATH $(find . -name *.java)
