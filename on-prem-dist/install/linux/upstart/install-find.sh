#!/bin/bash
# Copyright 2015 Hewlett-Packard Development Company, L.P.
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.

## INSTALLATION SETTINGS ##
## If you change these, you need to change them in find.conf as well
NAME="find"
BASE_DIR="/opt/$NAME"
HOME_DIR="$BASE_DIR/home"
USER="$NAME"
GROUP="$NAME"
##

## TODO - cd to directory script is in?

useradd $USER
mkdir $BASE_DIR
mkdir $HOME_DIR

cp ../../../$NAME.war $BASE_DIR

chown -R $USER:$GROUP $BASE_DIR

cp $NAME.conf /etc/init
chmod +x /etc/init/$NAME.conf

service $NAME start
