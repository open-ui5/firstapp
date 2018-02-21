#! /bin/sh
BUNDLES=org.eclipse.equinox.common@1:start,com.sap.org.apache.log4j@1:start,org.eclipse.osgi.services@1:start,org.eclipse.equinox.cm@1:start,com.sap.core.js.conf.agent@2:start
java -Dosgi.requiredJavaVersion=1.6 -Dosgi.install.area=../.. -Dorg.eclipse.equinox.simpleconfigurator.exclusiveInstallation=false -Declipse.ignoreApp=true -Dosgi.startLevel=2 -Dosgi.bundles=$BUNDLES -jar ../../plugins/com.sap.js.startup.jar
