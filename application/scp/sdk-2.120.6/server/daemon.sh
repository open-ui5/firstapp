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


# INIT_SECTION_BEGIN
# When the daemon is installed as a Unix service, this INIT SECTION is replaced
# with a list of variables with their values set.
# DO NOT ADD ANYTHING TO THIS SECTION, UNLESS IT IS SOMETHING RELATED WITH THE
# INITIALIZATIONS ALREADY IN IT. IF A NEW VARIABLE IS DEFINED IN THIS SECTION
# AND THIS VARIABLE IS NEEDED BY SOME OTHER SECTION OF THE SCRIPT, YOU SHOULD
# ADD IT TO THE LIST OF VARIABLES IN installLjsService FUNCTION. DO NOT ADD ANY
# FUNCTIONS TO THIS INIT SECTION.
#echo "DEBUG: JAVA_HOME is [$JAVA_HOME]"
# Set your JAVA_HOME here if it is not already set:
# for example:
# export JAVA_HOME=/usr/lib/java-1.6.0/jdk1.6.0_23
#echo "DEBUG: JAVA_HOME is [$JAVA_HOME]"
# end of java home example

SCRIPT="$0"

# SCRIPT may be an arbitrarily deep series of symlinks. Loop until we have the concrete path.
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  # Drop everything prior to ->
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

# determine kernel home
KERNEL_HOME=`dirname "$SCRIPT"`

# make KERNEL_HOME absolute
KERNEL_HOME=`cd "$KERNEL_HOME"; pwd`
CONFIG_DIR="$KERNEL_HOME/configuration"
TMP_DIR="$KERNEL_HOME/work/tmp"

CLASSPATH=

#  Append the osgi jar to the classpath
for file in "$KERNEL_HOME"/plugins/org.eclipse.equinox.launcher_*.jar
do
CLASSPATH="\"$KERNEL_HOME/plugins/${file##*/}\""
done

#  Create the classpath for bootstrapping the Server from all the JARs in lib
for file in "$KERNEL_HOME"/lib/*
do
if [[ "$file" == *.jar ]]
then
CLASSPATH=$CLASSPATH:"\"$KERNEL_HOME/lib/${file##*/}\""
fi
done

# make sure we have CLASSPATH set
if [ -z "$CLASSPATH" ]
then
echo No JAR files found in "$KERNEL_HOME/lib"
exit 1
fi

runShell=/bin/bash
javaCommand="java" # name of the Java launcher without the path
if [ ! -z "$JAVA_HOME" ]; then # $JAVA_HOME is not null
    javaExe="$JAVA_HOME/bin/$javaCommand"
elif [ ! -z $(which java) ]; then # $JAVA_HOME is null (zero string)
    javaExe=$(which java) # file name of the Java application launcher executable
fi
if [ -z "$JAVA_HOME" ] | [ -z "$javaExe" ]; then echo Unable to set JAVA_HOME environment variable; exit 5; fi

#check java version
JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
if [[ "$JAVA_VERSION" =~ 1\.[^67] ]]
then
    echo "JAVA version is not correct. The supported versions are 1.6 and 1.7. Current java is $JAVA_VERSION";
fi

if ! "$javaExe" -server 2>&1| grep -q -i Error:
then JAVA_VARS='-server'; fi
if "$javaExe" -version 2>&1| grep -q -i SAP
then JAVA_VARS=$JAVA_VARS' -XtraceFile=log/vm_@PID_trace.log';
else if [ -f "$KERNEL_HOME/lib/openejb-javaagent_4.5.2.jar" ];
then
       JAVA_VARS=$JAVA_VARS" -javaagent:\"$KERNEL_HOME/lib/openejb-javaagent_4.5.2.jar\"";
     fi
fi

ljsDir="${BASH_SOURCE[0]}";                                 # absolute path of the script directory
if([ -h "${ljsDir}" ]) then
  while([ -h "${ljsDir}" ]) do ljsDir=`readlink "${ljsDir}"`; done
fi
pushd . > /dev/null
script_filename=`basename "${ljsDir}"`
tmp_dir=`dirname "${ljsDir}"`
cd "$tmp_dir" > /dev/null
ljsDir=`pwd`;
popd  > /dev/null
scriptFile="$ljsDir/$script_filename"                           # the absolute, dereferenced path of this script file
applDir="$ljsDir"                                       # home directory of the service application
serviceName=$(basename $(echo ${ljsDir#*/}))"_Daemon"                    # service name
serviceNameLo=$(echo $serviceName|tr "[:upper:]" "[:lower:]")             # service name with the first letter in lowercase
UNAME_SYSTEM=`uname -s`
case "$UNAME_SYSTEM" in
   Darwin) # MacOS X uses BSD stat
      serviceUser=$(stat -f "%Su" "$scriptFile" )                # OS user name for the service
      serviceGroup=$(stat -f "%Sg" "$scriptFile" )               # OS group name for the service
      ;;
   *) # Linux uses GNU stat
      serviceUser=$(stat -c "%U" "$scriptFile" )                # OS user name for the service
      serviceGroup=$(stat -c "%G" "$scriptFile" )               # OS group name for the service
      ;;
esac
serviceUserHome="$applDir"                                 # home directory of the service user
serviceLogFile="$applDir/$serviceNameLo.log"               # log file for StdOut/StdErr
maxShutdownTime=60                                         # maximum number of seconds to wait for the daemon to terminate normally
pidFile="$applDir/$serviceNameLo.pid"                      # name of PID file (PID = process ID number)
javaCommandLineKeyWord="org.eclipse.equinox.launcher"      # a keyword that occurs on the commandline, used to detect an already running service process and to distinguish it from others
rcFileBaseName="rc$serviceNameLo"                          # basename of the "rc" symlink file for this script
rcFileName="/usr/local/sbin/$rcFileBaseName"               # full path of the "rc" symlink file for this script
etcInitDFile="/etc/init.d/$serviceNameLo"                  # symlink to this script from /etc/init.d
propsFile="$applDir/props.ini"
daemonExt="${ljsDir}/daemon_ext.sh"

# INIT_SECTION_END

# Executes a given command $1 as a $serviceUser
function executeAsServiceUser {
  currentUser=`whoami`
  if [ $currentUser == $serviceUser ]
    then # this script was run by serviceuser ... run it directly
      #echo -ne "DEBUG: executing command:\n$1\n"
      $runShell -c "$1"

      return $?
  fi
  # current user is not service user- drop priviledge
  # echo -ne "DEBUG: executing as $serviceUser:\n$1\n"
  su $serviceUser -s $runShell -c "$1"
  return $?
}

# Ensure that the tmp directory exists
executeAsServiceUser "mkdir -p \"$TMP_DIR\"" || return 1

# preserve the initial command and shift it
COMMAND=$1
shift

# set debug defaults
DEBUG_FLAG=
DEBUG_PORT=8000
SUSPEND=n

# this line is required for the regular expressions on line 174
# do not delete!
shopt -s extglob

# read any arguments passed to the script
while (($# > 0))
    do
        case $1 in
        -debug)
                DEBUG_FLAG=1
                if [[ "$2" == +([0-9]) ]]
                then
                    DEBUG_PORT=$2
                    shift;
                fi
                ;;
        -suspend)
                SUSPEND=y
                ;;
        *)
        esac
        shift
    done

# init debug opts
if [ "$DEBUG_FLAG" ]
    then
        DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$SUSPEND"
fi

# init params from props.ini
jvmParams="$DEBUG_OPTS -XX:ErrorFile=\"$KERNEL_HOME/log/error.log\""
jvmParams="$jvmParams -XX:HeapDumpPath=\"$KERNEL_HOME/log/heap_dump.hprof\""
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
            if [ -z "$jvmParams" ]
              then jvmParams="$line"
                else jvmParams="$jvmParams \"$line\""
            fi
        fi
    fi
    if [ "$line" == "#jvm" ]
    then
        read="true";
    fi
done < "$propsFile";

jvmParams="$jvmParams -classpath $CLASSPATH"

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
            if [ -z "$mainParams" ]
              then mainParams="$line"
              else mainParams="$mainParams $line"
            fi
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
      if [ -z "$programParams" ]
        then programParams="$line"
        else programParams="$programParams $line"
      fi
    fi
    if [ "$line" == "#program" ]
    then
        read="true";
    fi
done < "$propsFile";
# the loop skips the last line so add it
# programParams="$programParams $line";

#read the external params. These are passed after the service command
#The first param is sciped because it is ment for the service(start, stop, install, etc.)
externalParams=""
isFirst="true"
for var in $@
do
 if [ $isFirst == "false" ]
 then
    if [ -z "$externalParams" ]
      then externalParams="$var"
      else externalParams="$externalParams $var"
    fi
 fi
 isFirst="false";
done
JAVA_ARGS="$jvmParams $externalParams $mainParams $programParams"
javaArgs="$JAVA_VARS $JAVA_ARGS"                                  # arguments for Java launcher
javaCommandLine="\"$javaExe\" $javaArgs"                       # command line to start the Java service application
# echo -ne "DEBUG: \njavaCommandLine:[$javaCommandLine]\n"

# Returns 0 if the process with PID $1 is running.
function checkPidIsRunning {
   local checkPid="$1"
   if [ -z "$checkPid" -o "$checkPid" == " " ] # parameter validation
     then return 1
   fi
   ps -p "$checkPid" > /dev/null
   if [ $? -ne 0 ]
     then return 1
   fi
   return 0;
}

# Returns 0 if the process with PID $1 is our Java service process.
function checkPidIsOurService {
   local checkPid="$1"
   #get the command with which the process is started; awk 'NR>1' otputs the rows after the first;
   #this is necessary, because ps -p displays a first row with headers and on Mac there is no option for skipping the header line 
   local cmd=`ps -p $checkPid -o comm | awk 'NR>1'`
   echo $cmd | grep -q "/$javaCommand\>"
   if [ $? -ne 0 -a "$cmd" != "$javaCommand" ]
     then
         #printf "DEBUG: cmd %s \n is different then javaCommand %s \n" $cmd $javaCommand
       return 1
   fi

   ps -p $checkPid | grep -q "$javaCommandLineKeyword"
   if [ $? -ne 0 ]
     then
       #printf "DEBUG: %s\n %s\n" $javaCommandLineKeyword $tmp
       return 1
     else return 0
   fi
}

# Returns 0 if the service is running and sets the variable $ljsPid to the PID.
function getLjsPid {
   #echo "DEBUG: getLjsPid looking for file: $pidFile\n"
   if [ ! -f "$pidFile" ]
     then return 1
   fi
   ljsPid="$(<"$pidFile")"

   #printf "DEBUG: getLjsPid %s\n" $ljsPid


   #echo -ne "DEBUG: getLjsPid found pid: $ljsPid \n"
   checkPidIsRunning $ljsPid || return 1

   #printf "DEBUG: checkPidIsRunning %d\n" $?

   #echo -ne "DEBUG: getLjsPid pid $ljsPid is running \n"
   checkPidIsOurService $ljsPid || return 1

   #printf "DEBUG: checkPidIsOurService $ljsPid  %d\n" $?

   #echo -ne "DEBUG: getLjsPid pid $ljsPid is our service \n"
   return 0
 }

function runInConsoleMode {
   getLjsPid
   if [ $? -eq 0 ]
     then echo "$serviceName is already running"
     return 1
   fi
   cd "$applDir" || return 1

   rm -f \"$pidFile\"
   makeFileWritable "$serviceLogFile" || return 1
   local cmd="$javaCommandLine 2>>\"$serviceLogFile\""
   current_user=`whoami`
   if [ $current_user == $serviceUser ]
   then
      #echo -ne "DEBUG: exec $cmd\n"
      #echo -ne "DEBUG: shell $SHELL\n"
      $runShell -c "cd \"$applDir\" ; $cmd" || return 1
      #$cmd || return 1
   else
      su $serviceUser -s $runShell -c "cd \"$applDir\" ; $cmd" || return 1
   fi
   return 0;
 }

function makeFileWritable {
  local fileToBeWritable="$1"
  touch "$fileToBeWritable"
  chgrp "$serviceGroup" "$fileToBeWritable"
  chmod g+w "$fileToBeWritable"
  return 0;
}

function invokeExtension {
   if [ -f "${daemonExt}" ]; then
      [[ ! -x "${daemonExt}" ]] && chmod 755 "${daemonExt}"
      "${daemonExt}" $1
      rc=$?
      if [ ${rc} -ne 0 ]; then
         exit ${rc}
      fi
   fi
}

function startLjsProcess {
  cd "$applDir" || return 1
  #echo -ne "DEBUG: applDir=[$applDir]\n"
  #executeAsServiceUser "rm -f \"$pidFile\""
  rm -f \"$pidFile\"
  makeFileWritable "$serviceLogFile" || return 1
  makeFileWritable "$pidFile" || return 1
  read -t 1 password
  local cmd="echo $password | $javaCommandLine 2>>\"$serviceLogFile\" & echo \$! >\"$pidFile\""
  password=dummy
  #echo -ne "DEBUG: I will execute command: $cmd \n"
  executeAsServiceUser "$cmd" || return 1
  sleep 1.1 # wait for the pid file to be ready
  ljsPid="$(<"$pidFile")"
  if ! checkPidIsRunning $ljsPid
    then
      echo -ne "\n$serviceName start failed, see logfile: $serviceLogFile\n"
      return 1
    else
      echo -ne "\n$serviceName started.\n"
      #echo -ne "DEBUG: Process ID: $ljsPid Service user: $serviceUser\n"
      return 0;
  fi

}

 #returns 0 in case that succeed to start service or it was already started. The function also invokes the pre-start and post-start extensions(only if available) and if any of them fails the script exits with rc 1
 function StartLjs {
   getLjsPid
   if [ $? -eq 0 ]
    then
       echo -ne "$serviceName is already running. Process ID: $ljsPid\n"
       return 0
   fi
   invokeExtension pre_start
   echo -ne "Starting $serviceName\n"
   startLjsProcess
   if [ $? -ne 0 ]
     then
    return 1
     else
        invokeExtension post_start
    return 0
   fi
}

function stopLjsProcess {
  ljsPid="$(<"$pidFile")"
  ljsCmdLine=`echo /proc/$ljsPid/cmdline`
  ljsCommandPortPropCount=`echo $ljsCmdLine | grep "ljs.command.port" | wc -l`
  osgiConsolePortPropCount=`echo $ljsCmdLine | grep "\-console [a-zA-Z0-9]" | wc -l`
  local isLjsStoppedViaTelnet=1

  if [ $ljsCommandPortPropCount != "0" ] || [ $osgiConsolePortPropCount != "0" ]
    then
    stopLjsViaTelnetAndRemovePidFile $ljsPid
    isLjsStoppedViaTelnet=$?
  fi

  if [ $isLjsStoppedViaTelnet != "0" ]
  then
    killLjsSoftlyAndRemovePidFile $ljsPid
  fi

  return 0;
}

function stopLjsViaTelnetAndRemovePidFile {
  local ljsPid="$1"
  if [ -d "$applDir/tools/cmdclient" ]
    then
    cd "$applDir" || return 1
    local cmd="\"$javaExe\" -cp \"$applDir/tools/cmdclient/lib/com.sap.core.js.commandline.client.jar\" com.sap.core.js.command.client.TelnetCommandClient stopLJS"
    executeAsServiceUser "$cmd"
    for ((i=0; i<maxShutdownTime; i++))
    do
      checkPidIsRunning $ljsPid
      if [ $? -ne 0 ]
        then
        removePidFile
        return 0
      fi
      sleep 1.0
    done
    echo -e "\n$serviceName did not terminate within timeout of $maxShutdownTime seconds, sending SIGTERM..."
  fi

  return 1
}

function killLjsSoftlyAndRemovePidFile {
  local ljsPid="$1"
  checkPidIsRunning $ljsPid
  if [ $? -ne 0 ]; then
    echo "No $serviceName process was found. Stop aborted."
    return 0
  else
    kill -15 "$ljsPid" >/dev/null 2>&1
  fi

  sleeptime=45
  while [ $sleeptime -ge 0 ]; do
    checkPidIsRunning $ljsPid
    if [ $? -ne 0 ]; then
      removePidFile
      break
    fi

    if [ $sleeptime -gt 0 ]; then
      sleep 1
    fi

    sleeptime=`expr $sleeptime - 1 `
  done

  if [ -f "$pidFile" ]; then
    echo "Killing $serviceName with the PID: $ljsPid"
    kill -s KILL "$ljsPid"
    removePidFile
  fi

  return 0
}

function removePidFile {
  rm -f "$pidFile" >/dev/null 2>&1
  if [ $? != 0 ]; then
    if [ -w "$pidFile" ]; then
      cat /dev/null > "$pidFile"
    else
      echo "$serviceName stopped but the PID file could not be removed or cleared."
    fi
  fi
}

function StopLjs {
   getLjsPid
   if [ $? -ne 0 ]
     then
       echo -ne "$serviceName is not running\n"
       return 0
   fi
   invokeExtension pre_stop
   echo -ne "Stopping $serviceName \n"
   stopLjsProcess
   if [ $? -ne 0 ]
     then
       echo -ne "Unable to stop service $serviceName\n"
       return 1
     else
       echo -ne "Service $serviceName stopped\n"
       invokeExtension post_stop
       return 0;
   fi
}

function checkServiceStatus {
   echo -ne "Checking for $serviceName: "
   if getLjsPid
     then
       echo -ne "$serviceName is started.\n"
     else
       echo -ne "$serviceName is stopped.\n"
   fi
   return 0;
}

function checkRunAsRoot {
   if [[ $(/usr/bin/id -u) -ne 0 ]]
     then
       echo "Not running as root"
       return 1
   fi
   return 0
}

function installLjsService {
   egrep -i "$serviceGroup" /etc/group >/dev/null 2>&1
   if [ $? -ne 0 ] #if $serviceGroup doesn't exist, create it
     then
       echo "Creating group $serviceGroup"
       groupadd -r "$serviceGroup" || return 1
   fi

   egrep -i $serviceUser /etc/passwd >/dev/null 2>&1
   if [ $? -ne 0 ] # if $serviceUser doesn't exist, create it
   then
      echo Creating user $serviceUser
      useradd -r -c "user for $serviceName service" -g "$serviceGroup" -G users -d $serviceUserHome $serviceUser
   fi

   if test -x "/usr/bin/sed"
     then
        SED="/usr/bin/sed"
   elif test -x "/bin/sed"
     then
        SED="/bin/sed"
   else
     echo "Sed command not found!!!" && return 1
   fi
   $SED -n -e '1,/# INIT_SECTION_BEGIN/ p' -e '/# INIT_SECTION_END/,$ p' <"$scriptFile" >"$scriptFile.tmp"
   $SED "
/# INIT_SECTION_BEGIN$/ a\
runShell=\"$runShell\"\\
ljsDir=\"$ljsDir\"\\
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
KERNEL_HOME=\"$KERNEL_HOME\"\\
TMP_DIR=\"$TMP_DIR\"\\
CLASSPATH=\"$CLASSPATH\"\\
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
      $INS_SERV $serviceNameLo
      if [ $? -ne 0 ]
        then
          echo "$serviceName was not installed properly by $INS_SERV."
          return 1
      fi
   elif test -x $CHK_CONFIG; then
      $CHK_CONFIG --add $serviceNameLo
      if [ $? -ne 0 ]
        then
          echo "$serviceName was not installed properly by $CHK_CONFIG."
          return 1
      fi
   else
      echo "No facility to install service!!!" && return 1
   fi

   echo $serviceName installed.
   echo You may now use $rcFileName to call this script.
   return 0;

}

function uninstallLjsService {
   INS_SERV=/sbin/insserv
   CHK_CONFIG=/sbin/chkconfig
   if test -x $INS_SERV
   then
      $INS_SERV -r $serviceNameLo
      if [ $? -ne 0 ]
        then
          echo "$serviceName was not uninstalled properly by $INS_SERV."
          return 1
      fi
   elif test -x $CHK_CONFIG; then
      $CHK_CONFIG --del $serviceNameLo
      if [ $? -ne 0 ]
        then
          echo "$serviceName was not uninstalled properly by $CHK_CONFIG."
          return 1
      fi
   else
      echo "No facility to install service!!!" && return 1
   fi
   rm -f $rcFileName
   rm -f $etcInitDFile
   echo $serviceName uninstalled.
   return 0;
}

function main {
# analyse the cases of the first argument
case "$1" in
   console) # runs the Java program in console mode
      runInConsoleMode
      ;;
   start) # starts the Java program as a Linux service
      StartLjs
      ;;
   stop) # stops the Java program service
      StopLjs
      ;;
   restart) # stops and restarts the service
      StopLjs && StartLjs
      ;;
   status) # displays the service status
      checkServiceStatus
      ;;
   install)  # installs the service in the OS
      if ! checkRunAsRoot; then
        return 1
      fi
      installLjsService
      ;;
   uninstall) # uninstalls the service in the OS
      if ! checkRunAsRoot; then
        return 1
      fi
      uninstallLjsService
      ;;
   reinstall)  #reinstalls the service in case of changes
      if ! checkRunAsRoot; then
        return 1
      fi
      uninstallLjsService && installLjsService
      ;;
   *)
      echo "Usage: $0 {console|start|stop|restart|status|install|reinstall|uninstall}"
      exit 1
      ;;
 esac
}

main $COMMAND
