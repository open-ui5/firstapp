#!/bin/sh
DIR=`dirname $0`
java -cp "$DIR/lib/com.sap.core.js.commandline.client.jar" com.sap.core.js.command.client.TelnetCommandClient "$@"	
