#!/bin/bash
pid=`ps -ef | grep "java -jar Cheetah.jar"| grep -v grep | awk '{print $2}'`
if [ -n "$pid" ]; then
	kill -9 $pid
fi
java -jar Cheetah.jar