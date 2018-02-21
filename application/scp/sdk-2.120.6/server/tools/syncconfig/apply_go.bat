@echo off
java -version 2>&1 | findstr /I /R 1.[67] > nul
if errorlevel == 1 (
echo JAVA version is not correct. The supported versions are 1.6 and 1.7. 
goto end
)

java -server 2>&1 | findstr /I Error: > nul
if errorlevel == 1 (set JAVA_OPTS=-server)

set bundles=org.eclipse.equinox.common@1:start,com.sap.org.apache.log4j@1:start,org.eclipse.osgi.services@1:start,org.eclipse.equinox.cm@1:start,com.sap.core.js.conf.agent@2:start
@echo on
java %JAVA_OPTS% -Dosgi.requiredJavaVersion=1.6 -Dosgi.install.area=..\.. -Dorg.eclipse.equinox.simpleconfigurator.exclusiveInstallation=false -Declipse.ignoreApp=true -Dosgi.startLevel=2 -Dosgi.bundles=%bundles% -jar ..\..\plugins\com.sap.js.startup.jar
:end