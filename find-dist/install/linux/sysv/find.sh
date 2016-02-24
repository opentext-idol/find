#!/bin/bash

# © Copyright 2015 Hewlett Packard Enterprise Development, L.P. 
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.

## Installation defaults
NAME="find"
MYUSER="$NAME"
MYGROUP="$NAME"
BASEDIR="/opt/$NAME"
HOMEDIR="$BASEDIR/home"
PORT=8080
EXECUTABLE="$BASEDIR/$NAME.jar"
JAVA_BIN="/usr/bin/java"

## Don't change these
PRODUCT_NAME="HPE BI for Human Information"
LOCKFILE="/var/lock/$NAME.pid"
STARTUP_LOG="$BASE_DIR/console.log"
ARGS="-Dhp.find.home=$HOMEDIR -Dserver.port=$PORT -jar $EXECUTABLE -uriEncoding utf-8"
SLEEPTIME=2
##

# Wait for a process to terminate
waitForPid() {
	local PID=$1
	echo "Waiting for process" $PID "to terminate"
	while [ -e /proc/$PID ];
	do
		sleep $SLEEPTIME
	done
	echo "Process" $PID "has terminated"
	return 0
}

# Starts the server as $MYUSER
startServer() {
	echo "Attempting to start $PRODUCT_NAME"

	start-stop-daemon --start \
	--exec $JAVA_BIN \
	-m --pidfile "$LOCKFILE" \
	--user $MYUSER --group $MYGROUP --chuid $MYUSER \
	--chdir "$BASEDIR" \
	--startas "$JAVA_BIN" \
	-- $ARGS &> "$STARTUP_LOG" &
}

# Checks if process is running and returns:
# 0 - Running
# 1 - Not running
getStatus() {
	if [ -f $LOCKFILE ] # Check if pid file exists
	then
		if ps --no-headers -p `cat $LOCKFILE` > /dev/null # Check if process from pid is still running
		then
			return 0
		else
			return 1
		fi
	else
		return 1
	fi
}

# Stops the server and removes the pidfile
stopServer() {
	getStatus
	STATUS=$?

	if [ $STATUS -eq 0 ]
	then
		local PID=`cat $LOCKFILE`
		echo "Sending stop signal to $NAME"
		kill $PID

		echo "Waiting for process $PID to terminate"
		waitForPid $PID
		rm $LOCKFILE
	else
		echo "$NAME is not currently running"
	fi
	return 0
}

# Restarts the server
restartServer() {
	local PID=`cat $LOCKFILE`
	
	echo "Stopping $NAME"
	stopComponent

	echo "Starting $NAME"
	startComponent
	return 0
}

# Prints the server status (running/stopped) to the command line and returns:
# 0 - Running
# 1 - Stopped
printStatus() {
	getStatus
	STATUS=$?

	if [ $STATUS -eq 0 ]
	then
		local PID=`cat $LOCKFILE`
		echo "$NAME is running - process id $PID"
		return 0
	else
		echo "$NAME is not currently running"
		return 1
	fi
}

# Entrypoint
case $1 in
	start)
		startServer
	;;
	stop)
		stopServer
	;;
	restart)
		restartServer
	;;
	status)
		printStatus
esac
exit $?
