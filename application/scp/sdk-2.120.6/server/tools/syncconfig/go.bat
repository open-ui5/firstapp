@echo off

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
goto end
)

%JAVA_EXE% -server 2>&1 | findstr /I Error: > nul
if errorlevel == 1 (set JAVA_OPTS=-server)

%JAVA_EXE% -version 2>&1 | findstr /I SAP > nul
if not errorlevel == 1 (set JAVA_OPTS=%JAVA_OPTS% -XtraceFile=log/vm_@PID_trace.log)

REM read the lines between #jvm and #main for jvm params
set vmArgs=
set read=false
setlocal enabledelayedexpansion
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
%JAVA_EXE% %JAVA_OPTS% %vmArgs% %* %mainArgs% %programArgs%
@echo off
endlocal

:end
