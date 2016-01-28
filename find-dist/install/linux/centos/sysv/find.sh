#!/bin/bash

# chkconfig: 2345 70 30
# description: HPE Find Application

# Source function library.
. /etc/rc.d/init.d/functions

## Installation defaults
NAME="find"
MY_USER="${NAME}"
BASE_DIR="/opt/${NAME}"
HOME_DIR="${BASE_DIR}/home"
PORT=8080
EXECUTABLE="${BASE_DIR}/${NAME}.war"
JAVA_BIN="/usr/bin/java"

## Don't change these
PRODUCT_NAME="HPE Find"
ARGS=("-Dhp.find.home=${HOME_DIR}" "-Dserver.port=${PORT}" "-jar" "${EXECUTABLE}")
STARTUP_LOG="${BASE_DIR}/console.log"

[ -e /etc/sysconfig/${NAME} ] && . /etc/sysconfig/${NAME}

lockfile=/var/lock/subsys/${NAME}

start() {
    echo -n $"Starting ${PRODUCT_NAME}: "
    (
        exec daemon --pidfile "${lockfile}" --user "${MY_USER}" "${JAVA_BIN}" "${ARGS[@]}"
    ) > "${STARTUP_LOG}" 2>&1 &
    returnValue=$?
    echo
    [ ${returnValue} -eq 0 ] && touch ${lockfile}
    return ${returnValue}
}

stop() {
    echo -n $"Stopping ${PRODUCT_NAME}: "

    killproc -p "${lockfile}" "${NAME}" -TERM
    returnValue=$?
    echo
    [ ${returnValue} -eq 0 ] && rm -f ${lockfile}
    return ${returnValue}
}

restart() {
    stop
    start
}

reload() {
    restart
}

force_reload() {
    restart
}

rh_status() {
    # run checks to determine if the service is running or use generic status
    status ${NAME}
}

rh_status_q() {
    rh_status >/dev/null 2>&1
}


case "$1" in
    start)
        rh_status_q && exit 0
        $1
        ;;
    stop)
        rh_status_q || exit 0
        $1
        ;;
    restart)
        $1
        ;;
    reload)
        rh_status_q || exit 7
        $1
        ;;
    force-reload)
        force_reload
        ;;
    status)
        rh_status
        ;;
    condrestart|try-restart)
        rh_status_q || exit 0
        restart
        ;;
    *)
        echo $"Usage: $0 {start|stop|status|restart|condrestart|try-restart|reload|force-reload}"
        exit 2
esac
exit $?
