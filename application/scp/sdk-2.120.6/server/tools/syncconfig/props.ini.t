#jvm

-XX:+HeapDumpOnOutOfMemoryError
-XX:+DisableExplicitGC
@Xms@
@Xmx@
-XX:PermSize=@XXPermSize@
-XX:MaxPermSize=@XXMaxPermSize@
-Dcom.sun.management.jmxremote.port=@jmxremote.port@
-Dcom.sun.management.jmxremote.authenticate=@jmxremote.authenticate@
-Dcom.sun.management.jmxremote.ssl=@jmxremote.ssl@
@osgi.requiredJavaVersion@
-DuseNaming=osgi
-Dosgi.install.area=.
-Djava.io.tmpdir=./work/tmp
-Djava.endorsed.dirs=lib/endorsed
-Dorg.eclipse.equinox.simpleconfigurator.exclusiveInstallation=false
-Dcom.sap.core.process=ljs_node
-Declipse.ignoreApp=true
-Dosgi.noShutdown=true
-Dosgi.framework.activeThreadType=normal
-Dosgi.embedded.cleanupOnSave=true
-Dosgi.usesLimit=30
-Djava.awt.headless=true

#main
org.eclipse.equinox.launcher.Main

#program
-console
