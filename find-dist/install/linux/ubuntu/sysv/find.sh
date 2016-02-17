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
NAME="Find"
MY_USER="${NAME}"
MY_GROUP="${NAME}"
BASE_DIR="/opt/${NAME}"
HOME_DIR="${BASE_DIR}/home"
PORT=8080
EXECUTABLE="${BASE_DIR}/${NAME}.war"
JAVA_BIN="/usr/bin/java"

## Don't change these
PRODUCT_NAME="HPE Find"
LOCKFILE="/var/lock/${NAME}.pid"
STARTUP_LOG="${BASE_DIR}/console.log"
ARGS=("-Dhp.find.home=${HOME_DIR}" "-Dserver.port=${PORT}" "-jar" "${EXECUTABLE}")
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

# Starts the server as ${MY_USER}
startServer() {
	echo "Attempting to start ${PRODUCT_NAME}"

	if command -v start-stop-daemon >/dev/null; then
		start-stop-daemon --start \
		--exec "${JAVA_BIN}" \
		-m --pidfile "${LOCKFILE}" \
		--user "${MY_USER}" --group "${MY_GROUP}" --chuid "${MY_USER}" \
		--chdir "${BASE_DIR}" \
		--startas "${JAVA_BIN}" \
		-- "${ARGS[@]}" > "${STARTUP_LOG}" 2>&1
	else
		if [ -e "${LOCKFILE}" ]; then
			echo >&2 "ERROR: ${LOCKFILE} exists for PID $( cat "${LOCKFILE}" ), subsystem locked"
			return 1
		fi
		(
			# Execute in a sub-shell...
			echo "${$}" >"${LOCKFILE}"
			exec daemon --user "${MY_USER}" "${JAVA_BIN}" "${ARGS[@]}"
		) > "${STARTUP_LOG}" 2>&1
		return ${?}
	fi
}

# Checks if process is running and returns:
# 0 - Running
# 1 - Not running
getStatus() {
	# Check if pid file exists
	if [ -f ${LOCKFILE} ]; then
		 # Check if process from pid is still running
		 return "$( ps --no-headers -p < "${LOCKFILE}" >/dev/null )"
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
	stopComponent

	echo "Starting ${NAME}"
	startComponent
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
