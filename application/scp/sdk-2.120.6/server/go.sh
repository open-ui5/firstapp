#!/bin/sh

if [ -e ./daemon.sh ] 
then
 ./daemon.sh console "$@";
else 
 echo "Daemon script is not available"
 exit 1
fi

exit 0
