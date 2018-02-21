#!/bin/bash

#
# /etc/init.d/LJS

# 

# chkconfig: 345 85 15
# description: LJS Service daemon providing LJS standalone services
# 
### BEGIN INIT INFO
# Provides:          LJS
# Required-Start:    $syslog $local_fs $network $remote_fs
# Should-Start:      $time
# Required-Stop:     $syslog $local_fs $network $remote_fs
# Should-Stop:
# Default-Start:     3 4 5
# Default-Stop:      0 1 2 6
# Short-Description: LJS Daemon
# Description:       A Java daemon for the Lean Java Server
### END INIT INFO

#
# Note on runlevels:
# 0 - halt/poweroff 			6 - reboot
# 1 - single user			2 - multiuser without network exported
# 3 - multiuser w/ network (text mode)  5 - multiuser w/ network and X11 (xdm)
# 
# Note on script names:
# http://www.linuxbase.org/spec/refspecs/LSB_1.3.0/gLSB/gLSB/scrptnames.html
# A registry has been set up to manage the init script namespace.
# http://www.lanana.org/
# Please use the names already registered or register one or use a
# vendor prefix.


# Check for missing binaries (stale symlinks should not happen)
# Note: Special treatment of stop for LSB conformance
# INIT_SECTION_BEGIN
if [ -z "$JAVA_HOME" ]; then                               # if JAVA_HOME is undefined
   if [ -f /usr/share/java-utils/java-functions ]; then
      . /usr/share/java-utils/java-functions; set_jvm      # JPackage standard method to set JAVA_HOME
   fi
fi
	javaCommand="java"                                         # name of the Java launcher without the path
if [ ! -z "$JAVA_HOME" ]; then 
	javaExe="$JAVA_HOME/bin/$javaCommand"
elif [ ! -z $(which java) ]; then
	javaExe=$(which java)                                      # file name of the Java application launcher executable
fi
if [ -z "$JAVA_HOME" ] | [ -z "$javaExe" ]; then echo Unable to set JAVA_HOME environment variable; exit 5; fi

#check java version
JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
if [[ "$JAVA_VERSION" =~ 1.[^67] ]]
then 
	echo "JAVA version is not correct. The supported versions are 1.6 and 1.7. Current java is $JAVA_VERSION"; 
	exit 5;
fi

if ! $javaExe -server 2>&1| grep -q -i Error:
then JAVA_VARS='-server'; fi
if $javaExe -version 2>&1| grep -q -i SAP
then JAVA_VARS=$JAVA_VARS' -XtraceFile=log/vm_@PID_trace.log'; fi
scriptDir="${BASH_SOURCE[0]}";                                 # absolute path of the script directory
if([ -h "${scriptDir}" ]) then
  while([ -h "${scriptDir}" ]) do scriptDir=`readlink "${scriptDir}"`; done
fi
pushd . > /dev/null
script_filename=`basename ${scriptDir}`
cd `dirname ${scriptDir}` > /dev/null
scriptDir=`pwd`;
popd  > /dev/null
scriptFile="$scriptDir/$script_filename"                           # the absolute, dereferenced path of this script file
applDir="$scriptDir"                                       # home directory of the service application
serviceName=$(basename $(echo ${scriptDir#*/}))"_Daemon"                    # service name
serviceNameLo=$(echo $serviceName|tr "[:upper:]" "[:lower:]")             # service name with the first letter in lowercase
serviceUser=$(ls -l "$scriptFile" | awk '{ print $3 }')                                    # OS user name for the service
serviceUserHome="$applDir"                                 # home directory of the service user
serviceGroup=$(ls -l "$scriptFile" | awk '{ print $4 }')                                    # OS group name for the service
serviceLogFile="$applDir/$serviceNameLo.log"               # log file for StdOut/StdErr
maxShutdownTime=60                                         # maximum number of seconds to wait for the daemon to terminate normally
pidFile="$applDir/$serviceNameLo.pid"                      # name of PID file (PID = process ID number)
javaCommandLineKeyword="com.sap.js.startup.jar"                    # a keyword that occurs on the commandline, used to detect an already running service process and to distinguish it from others
rcFileBaseName="rc$serviceNameLo"                          # basename of the "rc" symlink file for this script
rcFileName="/usr/local/sbin/$rcFileBaseName"               # full path of the "rc" symlink file for this script
etcInitDFile="/etc/init.d/$serviceNameLo"                  # symlink to this script from /etc/init.d
propsFile="$applDir/props.ini"

		
# INIT_SECTION_END
# read content from #jvm to #main section for JVM params
while read line;
do 
	if [ "$line" == "#main" ]
	then
		read="false";
	fi
	if [ "$read" == "true" ]
	then
		if test -z "$line"
		then
		echo "skip" >> /dev/null
		else
			line=$(echo $line|sed 's/"/\\"/g')
			jvmParams="$jvmParams \"$line\"";
		fi
	fi
	if [ "$line" == "#jvm" ]
	then
		read="true";
	fi	
done < "$propsFile";
# read content from #main to #program section for mian params
while read line;
do 
	if [ "$line" == "#program" ]
	then
		read="false";
	fi
	if [ "$read" == "true" ]
	then
		if test -z "$line"
		then
		echo "skip" >> /dev/null
		else
			line=$(echo $line|sed 's/"/\\"/g')
			mainParams="$mainParams $line";
		fi
	fi
	if [ "$line" == "#main" ]
	then
		read="true";
	fi	
done < "$propsFile";
read="false";
# read content from #program section to the eof for program params
while read line;
do 
	if [ "$read" == "true" ]
	then
		programParams="$programParams $line";
	fi
	if [ "$line" == "#program" ]
	then
		read="true";
	fi	
done < "$propsFile";
# the loop skips the last line so add it
programParams="$programParams $line";
#read the external params. These are passed after the service command
#The first param is sciped because it is ment for the service(start, stop, install, etc.)
externalParams=""
isFirst="true"
for var in $@
do 
 if [ $isFirst == "false" ]
 then
	externalParams="$externalParams $var";
 fi
 isFirst="false";
done
JAVA_ARGS="$jvmParams $externalParams $mainParams $programParams"

javaArgs="$JAVA_VARS $JAVA_ARGS"                                  # arguments for Java launcher
javaCommandLine="$javaExe $javaArgs"                       # command line to start the Java service application

# providing start_daemon, killproc, pidofproc, 
# log_success_msg, log_failure_msg and log_warning_msg.
# This is currently not used by UnitedLinux based distributions and
# not needed for init scripts for UnitedLinux only. If it is used,
# the functions from rc.status should not be sourced or used.
#. /lib/lsb/init-functions

# Shell functions sourced from /etc/rc.status:
#      rc_check         check and set local and overall rc status
#      rc_status        check and set local and overall rc status
#      rc_status -v     be verbose in local rc status and clear it afterwards
#      rc_status -v -r  ditto and clear both the local and overall rc status
#      rc_status -s     display "skipped" and exit with status 3
#      rc_status -u     display "unused" and exit with status 3
#      rc_failed        set local and overall rc status to failed
#      rc_failed <num>  set local and overall rc status to <num>
#      rc_reset         clear both the local and overall rc status
#      rc_exit          exit appropriate to overall rc status
#      rc_active        checks whether a service is activated by symlinks

# Use the SUSE rc_ init script functions;
# emulate them on LSB, RH and other systems

# Default: Assume sysvinit binaries exist
start_daemon() { return /sbin/start_daemon ${1+"$@"}; }
killproc()     { return /sbin/killproc     ${1+"$@"}; }
pidofproc()    { return /sbin/pidofproc    ${1+"$@"}; }
checkproc()    { return /sbin/checkproc    ${1+"$@"}; }
if test -e /etc/rc.status; then
    # SUSE rc script library
    . /etc/rc.status
else
    export LC_ALL=POSIX
    _cmd=$1
    declare -a _SMSG
    if test "${_cmd}" = "status"; then
	_SMSG=(running dead dead unused unknown reserved)
	_RC_UNUSED=3
    else
	_SMSG=(done failed failed missed failed skipped unused failed failed reserved)
	_RC_UNUSED=6
    fi
    if test -e /lib/lsb/init-functions; then
	# LSB    
    	. /lib/lsb/init-functions
	echo_rc()
	{
	    if test ${_RC_RV} = 0; then
		log_success_msg "  [${_SMSG[${_RC_RV}]}] "
	    else
		log_failure_msg "  [${_SMSG[${_RC_RV}]}] "
	    fi
	}
	# TODO: Add checking for lockfiles
	checkproc() { return pidofproc ${1+"$@"} >/dev/null 2>&1; }
    elif test -e /etc/init.d/functions; then
	# RHAT
	. /etc/init.d/functions
	echo_rc()
	{
	    #echo -n "  [${_SMSG[${_RC_RV}]}] "
	    if test ${_RC_RV} = 0; then
		success "  [${_SMSG[${_RC_RV}]}] "
	    else
		failure "  [${_SMSG[${_RC_RV}]}] "
	    fi
	}
	checkproc() { return status ${1+"$@"}; }
	start_daemon() { return daemon ${1+"$@"}; }
    else
	# emulate it
	echo_rc() { echo "  [${_SMSG[${_RC_RV}]}] "; }
    fi
    rc_reset() { _RC_RV=0; }
    rc_failed()
    {
	if test -z "$1"; then 
	    _RC_RV=1;
	elif test "$1" != "0"; then 
	    _RC_RV=$1; 
    	fi
	return ${_RC_RV}
    }
    rc_check()
    {
	return rc_failed $?
    }	
    rc_status()
    {
	rc_failed $?
	if test "$1" = "-r"; then _RC_RV=0; shift; fi
	if test "$1" = "-s"; then rc_failed 5; echo_rc; rc_failed 3; shift; fi
	if test "$1" = "-u"; then rc_failed ${_RC_UNUSED}; echo_rc; rc_failed 3; shift; fi
	if test "$1" = "-v"; then echo_rc; shift; fi
	if test "$1" = "-r"; then _RC_RV=0; shift; fi
	return ${_RC_RV}
    }
    rc_exit() { exit ${_RC_RV}; }
    rc_active() 
    {
	if test -z "$RUNLEVEL"; then read RUNLEVEL REST < <(/sbin/runlevel); fi
	if test -e /etc/init.d/S[0-9][0-9]${1}; then return 0; fi
	return 1
    }
fi

# Reset status of this service
rc_reset

# Return values acc. to LSB for all commands but status:
# 0	  - success
# 1       - generic or unspecified error
# 2       - invalid or excess argument(s)
# 3       - unimplemented feature (e.g. "reload")
# 4       - user had insufficient privileges
# 5       - program is not installed
# 6       - program is not configured
# 7       - program is not running
# 8--199  - reserved (8--99 LSB, 100--149 distrib, 150--199 appl)
# 
# Note that starting an already running service, stopping
# or restarting a not-running service as well as the restart
# with force-reload (in case signaling is not supported) are
# considered a success.
# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable {
   sudo -u $serviceUser $SHELL -c "touch $1" #|| return 1
   sudo -u $serviceUser $SHELL -c "chgrp $serviceGroup $1" #|| return 1
   sudo -u $serviceUser $SHELL -c "chmod g+w $1" #|| return 1
   return 0;
}

# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if [ ! -e /proc/$pid ]; then return 1; fi
   return 0; }

# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
   local pid="$1"
   local cmd="$(ps -p $pid --no-headers -o comm)"
   if [ "$cmd" != "$javaCommand" -a "$cmd" != "$javaCommand.bin" ]; then return 1; fi
   grep -q --binary -F "$javaCommandLineKeyword" /proc/$pid/cmdline
   if [ $? -ne 0 ]; then return 1; fi
   return 0; }

# Returns 0 when the service is running and sets the variable $servicePid to the PID.
function getServicePid {
   if [ ! -f "$pidFile" ]; then return 1; fi
   servicePid="$(<"$pidFile")"
   checkProcessIsRunning $servicePid || return 1
   checkProcessIsOurService $servicePid || return 1
   return 0; }

function startServiceProcess {
   cd "$applDir" || return 1
   sudo -u $serviceUser $SHELL -c "rm -f \"$pidFile\""
   makeFileWritable "$pidFile" || return 1
   makeFileWritable "$serviceLogFile" || return 1
   local cmd="setsid $javaCommandLine >>\"$serviceLogFile\" 2>&1 & echo \$! >\"$pidFile\""
   sudo -u $serviceUser $SHELL -c "$cmd" ||  su $serviceUser -c "$cmd" || return 1
   sleep 1.0
   servicePid="$(<"$pidFile")"
   if checkProcessIsRunning $servicePid; then :; else
      echo -ne "\n$serviceName start failed, see logfile."
      return 1
      fi
   return 0; }

function stopServiceProcess {
#   kill $servicePid || return 1
   if [ -d "$applDir/tools/cmdclient" ]; then
	cd "$applDir" || return 1
	local cmd="$javaExe -cp \"$applDir/tools/cmdclient/lib/com.sap.core.js.commandline.client.jar\" com.sap.core.js.command.client.TelnetCommandClient stopLJS"
	sudo -u $serviceUser $SHELL -c "$cmd"
	for ((i=0; i<maxShutdownTime; i++)); do
	checkProcessIsRunning $servicePid
	if [ $? -ne 0 ]; then
		sudo -u $serviceUser $SHELL -c "rm -f \"$pidFile\""
		return 0
		fi
	sleep 1.0
	done
	echo -e "\n$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   fi
   kill -s KILL $servicePid || return 1
   }

function runInConsoleMode {
   getServicePid
   if [ $? -eq 0 ]; then echo "$serviceName is already running"; return 1; fi
   cd "$applDir" || return 1
   $SHELL -c "$javaCommandLine" || return 1
   if [ $? -eq 0 ]; then return 1; fi
   return 0; }

function startService {
   getServicePid
   if [ $? -eq 0 ]; then echo -n "$serviceName is already running"; rc_failed 0; rc_status -v; return 0; fi
   echo -n "Starting $serviceName   "
   startServiceProcess
   if [ $? -ne 0 ]; then rc_failed 1; rc_status -v; return 1; fi
   rc_failed 0
   rc_status -v
   return 0; }

function stopService {
   getServicePid
   if [ $? -ne 0 ]; then echo -n "$serviceName is not running"; rc_failed 0; rc_status -v; return 0; fi
   echo -n "Stopping $serviceName   "
   stopServiceProcess
   if [ $? -ne 0 ]; then rc_failed 1; rc_status -v; return 1; fi
   rc_failed 0
   rc_status -v
   return 0; }

function checkServiceStatus {
   echo -n "Checking for $serviceName:   "
   if getServicePid; then
      rc_failed 0
    else
      rc_failed 3
      fi
   rc_status -v
   return 0; }

function checkRunAsRoot {
   if [[ $(/usr/bin/id -u) -ne 0 ]]; then
       echo "Not running as root"
       return 1
   fi
   return 0; }
   
function installService {
   if ! checkRunAsRoot; then 
      return 1
   fi
   getent group $serviceGroup >/dev/null 2>&1
   if [ $? -ne 0 ]; then
      echo Creating group $serviceGroup
      groupadd -r $serviceGroup || return 1
      fi
   id -u $serviceUser >/dev/null 2>&1
   if [ $? -ne 0 ]; then
      echo Creating user $serviceUser
      useradd -r -c "user for $serviceName service" -g $serviceGroup -G users -d $serviceUserHome $serviceUser
    fi
	if test -x "/usr/bin/sed"; then
	    SED="/usr/bin/sed"
	elif test -x "/bin/sed"; then
	    SED="/bin/sed"
	else
	    echo "Sed command not found!!!" && return 1
	fi
	$SED -n -e '1,/# INIT_SECTION_BEGIN/ p' -e '/# INIT_SECTION_END/,$ p' <"$scriptFile" >"$scriptFile.tmp"
	$SED "
/# INIT_SECTION_BEGIN$/ a\
scriptDir=\"$scriptDir\"\\
script_filename=\"$script_filename\"\\
scriptFile=\"$scriptFile\"\\
applDir=\"$applDir\"\\
serviceName=\"$serviceName\"\\
serviceNameLo=\"$serviceNameLo\"\\
serviceUser=\"$serviceUser\"\\
serviceUserHome=\"$serviceUserHome\"\\
serviceGroup=\"$serviceGroup\"\\
serviceLogFile=\"$serviceLogFile\"\\
maxShutdownTime=\"$maxShutdownTime\"\\
javaExe=\"$javaExe\"\\
javaCommand=\"$javaCommand\"\\
JAVA_VARS=\"$JAVA_VARS\"\\
pidFile=\"$pidFile\"\\
propsFile=\"$propsFile\"\\
javaCommandLineKeyword=\"$javaCommandLineKeyword\"\\
rcFileBaseName=\"$rcFileBaseName\"\\
rcFileName=\"$rcFileName\"\\
etcInitDFile=\"$etcInitDFile\"
" "$scriptFile.tmp" >"$rcFileName"

   ln -s $rcFileName $etcInitDFile || return 1
   rm -f "$scriptFile.tmp"
   chmod 755 $rcFileName
   
   INS_SERV=/sbin/insserv
   CHK_CONFIG=/sbin/chkconfig
   if test -x $INS_SERV; then
      $INS_SERV $serviceNameLo || return 1
   elif test -x $CHK_CONFIG; then
      $CHK_CONFIG --add $serviceNameLo || return 1
   else
      echo "No facility to install service!!!" && return 1
   fi
   echo $serviceName installed.
   echo You may now use $rcFileBaseName to call this script.
   return 0; }

function uninstallService {

   INS_SERV=/sbin/insserv
   CHK_CONFIG=/sbin/chkconfig
   if test -x $INS_SERV; then
      $INS_SERV -r $serviceNameLo || return 1
   elif test -x $CHK_CONFIG; then
      $CHK_CONFIG --del $serviceNameLo || return 1
   else
      echo "No facility to install service!!!" && return 1
   fi
   rm -f $rcFileName
   rm -f $etcInitDFile
   echo $serviceName uninstalled.
   return 0; }

function main {
   rc_reset
   case "$1" in
      console)                                             # runs the Java program in console mode
         runInConsoleMode
         ;;
      start)                                               # starts the Java program as a Linux service
         startService
         ;;
      stop)                                                # stops the Java program service
         stopService
         ;;
      restart)                                             # stops and restarts the service
         stopService && startService
         ;;
      status)                                              # displays the service status
         checkServiceStatus
         ;;
      install)                                             # installs the service in the OS
         installService
         ;;
      reinstall)                                           #reinstalls the service in case of changes
         uninstallService && installService
		 ;;
      uninstall)                                           # uninstalls the service in the OS
         uninstallService
         ;;
      *)
         echo "Usage: $0 {console|start|stop|restart|status|install|reinstall|uninstall}"
         exit 1
         ;;
      esac
   rc_exit; }

main $1
