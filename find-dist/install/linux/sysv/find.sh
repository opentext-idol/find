#!/bin/bash
#
# © Copyright 2015 Hewlett Packard Enterprise Development, L.P. 
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
#
# find		Start up the Find server daemon
#
# chkconfig: 2345 70 30
# description: Find init script
#
# processname: Find
# pidfile: /var/lock/${NAME}.pid

## Installation defaults
NAME="find"
FIND_USER="${NAME}"
FIND_GROUP="${NAME}"
FIND_INSTALL_DIR="/opt/${NAME}"
FIND_HOME_DIR="${FIND_INSTALL_DIR}/home"
FIND_PORT=8080
EXECUTABLE="${FIND_INSTALL_DIR}/${NAME}.war"
JAVA_BIN="/usr/bin/java"

## Don't change these
PRODUCT_NAME="HPE BI for Human Information"
LOCKFILE="/var/lock/${NAME}.pid"
STARTUP_LOG="${FIND_INSTALL_DIR}/console.log"
ARGS=("-Dhp.find.home=${FIND_HOME_DIR}" "-Dserver.port=${FIND_PORT}" "-jar" "${EXECUTABLE}")
SLEEP_TIME=2
##

# Wait for a process to terminate
waitForPid() {
	local -i pid="${1}"

	echo "Waiting for process ${pid} to terminate"

	while [ -e "/proc/${pid}" ]; do
		sleep ${SLEEP_TIME}
	done

	echo "Process ${pid} has terminated"

	return 0
}

# Starts the server as ${FIND_USER}
startServer() {
	echo "Attempting to start ${PRODUCT_NAME}"

	if [ -e "${LOCKFILE}" ]; then
		local PID
		PID=$(cat ${LOCKFILE})
		if [ -e /proc/${PID} ]; then
			echo "${NAME} is already running - process id ${PID}"
			return 1;
		fi

		rm -f "${LOCKFILE}"
	fi

	touch ${LOCKFILE} || return 1
	chgrp ${FIND_GROUP} ${LOCKFILE} || return 1
	chmod g+w ${LOCKFILE} || return 1

	local cmd
	cmd="nohup ${JAVA_BIN} ${ARGS[@]} >>${STARTUP_LOG} 2>&1 & echo \$! >${LOCKFILE}"
	su -m "${FIND_USER}" -s "${SHELL}" -c "${cmd}" || return 1
	printStatus
	return 0;
}

# Checks if process is running and returns:
# 0 - Running
# 1 - Not running
getStatus() {
	# Check if pid file exists
	if [ -f ${LOCKFILE} ]; then
		# Check if process from pid is still running
		ps --no-headers -p $( cat "${LOCKFILE}" ) >/dev/null 2>&1
		return ${?}
	fi

	return 1
}

# Stops the server and removes the pidfile
stopServer() {
	getStatus
	STATUS=$?

	if [ ${STATUS} -eq 0 ]
	then
		local PID
		PID=$(cat ${LOCKFILE})

		echo "Sending stop signal to ${NAME}"
		kill "${PID}"

		echo "Waiting for process ${PID} to terminate"
		waitForPid "${PID}"
		rm ${LOCKFILE}
	else
		echo "${NAME} is not currently running"
	fi
	return 0
}

# Restarts the server
restartServer() {
	local PID
	PID=$(cat ${LOCKFILE})
	
	echo "Stopping ${NAME}"
	stopServer

	echo "Starting ${NAME}"
	startServer
	return 0
}

# Prints the server status (running/stopped) to the command line and returns:
# 0 - Running
# 1 - Stopped
printStatus() {
	getStatus
	STATUS=$?

	if [ ${STATUS} -eq 0 ]
	then
		local PID
		PID=$(cat ${LOCKFILE})
		echo "${NAME} is running - process id ${PID}"
		return 0
	else
		echo "${NAME} is not currently running"
		return 1
	fi
}

# Entry point
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
