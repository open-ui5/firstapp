@echo off

rem set defaults
set DEBUG_OPTS=
set SUSPEND=n
set DEBUG_PORT=8000

rem loop through the startup options
:startOptionLoop
  	if "%~1"=="" goto endStartOptionLoop
	if "%~1"=="-debug"             goto debug
	if "%~1"=="-suspend"           goto suspend
	
	:continueStartOptionLoop
		shift
		goto startOptionLoop
		
	:debug
    set DEBUG_FLAG=1
    set PORT_CANDIDATE=%~2
    if not "%PORT_CANDIDATE:~0,1%"=="-" (
      set DEBUG_PORT=%PORT_CANDIDATE%
      shift
    )
	goto continueStartOptionLoop

	:suspend
	set SUSPEND=y
	goto continueStartOptionLoop

:endStartOptionLoop


  
rem Set JAVA_HOME in order to use different java than the default one on the system

set JAVA_EXE="java"
if defined JAVA_HOME set JAVA_EXE="%JAVA_HOME%\bin\java"

%JAVA_EXE% -version
if errorlevel == 1 (
@echo JAVA HOME is not set correctly.
goto end
)

%JAVA_EXE% -version 2>&1 | findstr /I /R 1.[67] > nul
if errorlevel == 1 (
	@echo JAVA version is not correct. The supported versions are 1.6 and 1.7.
)

%JAVA_EXE% -server 2>&1 | findstr /I Error: > nul
if errorlevel == 1 (set JAVA_OPTS=-server)

SET ROOT_DIR=%~dp0%

set KERNEL_HOME=
set CONFIG_DIR=
set TMP_DIR=
set CLASSPATH=

for %%I in ("%ROOT_DIR%.") do set KERNEL_HOME=%%~dpfsI
set TMP_DIR=%KERNEL_HOME%\work\tmp
if not exist "%TMP_DIR%" mkdir "%TMP_DIR%"

%JAVA_EXE% -version 2>&1 | findstr /I SAP > nul
if not errorlevel == 1 (
  set JAVA_OPTS=%JAVA_OPTS% -XtraceFile=log/vm_@PID_trace.log
) else if exist "%KERNEL_HOME%\lib\openejb-javaagent_4.5.2.jar" (
  set JAVA_OPTS=%JAVA_OPTS% -javaagent:"%KERNEL_HOME%\lib\openejb-javaagent_4.5.2.jar"
)

rem Construct the CLASSPATH list from the Kernel lib directory.
for %%G in ("%KERNEL_HOME%\lib\*.jar") do call :AppendToClasspath "%%G"
for %%G in ("%KERNEL_HOME%\plugins\org.eclipse.equinox.launcher_*.jar") do call :AppendToClasspath "%%G"

rem Remove leading semi-colon if present
if "%CLASSPATH:~0,1%"==";" set CLASSPATH=%CLASSPATH:~1%

if not "%DEBUG_FLAG%"=="" set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=%DEBUG_PORT%,server=y,suspend=%SUSPEND%

REM read the lines between #jvm and #main for jvm params
set vmArgs=%DEBUG_OPTS%
set read=false
setlocal enabledelayedexpansion

set vmArgs=%vmArgs% -XX:ErrorFile="%KERNEL_HOME%\log\error.log"
set vmArgs=%vmArgs% -XX:HeapDumpPath="%KERNEL_HOME%\log\heap_dump.hprof"

FOR /F "tokens=* delims=." %%1 in ('type props.ini') do (
	if "%%1" equ "#main" (
		set read=false
	)
	if "!read!" equ "true" (
		if "%%1" equ " " (
			rem skip
		) else (
			SET "vmArgs=!vmArgs! "%%1""
		)
	)
	if "%%1" equ "#jvm" (
		set read=true
	)	
)

set vmArgs=%vmArgs% -classpath "%CLASSPATH%"

REM read the lines between #main and #program for main params
set mainArgs=
setlocal enabledelayedexpansion
FOR /F "tokens=* delims=." %%1 in ('type props.ini') do (
	if "%%1" equ "#program" (
		set read=false
	)
	if "!read!" equ "true" (
		SET "mainArgs=!mainArgs! %%1"
	)
	if "%%1" equ "#main" (
		set read=true
	)	
)

REM read the lines between #program and eof for program params
set programArgs=
setlocal enabledelayedexpansion
FOR /F "tokens=* delims=." %%1 in ('type props.ini') do (
	if "!read!" equ "true" (
		SET "programArgs=!programArgs! %%1"
	)
	if "%%1" equ "#program" (
		set read=true
	)	
)

@echo on
%JAVA_EXE% %JAVA_OPTS% %vmArgs% %mainArgs% %programArgs%
@echo off
endlocal

:AppendToClasspath
  set CLASSPATH=%CLASSPATH%;%~1
  goto :eof

:end
