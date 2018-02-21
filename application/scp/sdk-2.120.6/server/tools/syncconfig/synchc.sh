
# stop the LJS
#java -cp tools/cmdclient/lib/com.sap.js.commandline.client.jar com.sap.js.command.client.TelnetCommandClient stopLJS
#sleep 2s
#echo should be stopped now

# SYNCHRONIZE the configuration
ant -buildfile ./tools/syncconfig/synch.xml 

# start the LJS 
#nohup ./go.sh &
#echo run go.sh
#sudo -u root /bin/bash -c ./go.sh &

#trigger configuration synch
java -cp ./tools/cmdclient/lib/com.sap.core.js.commandline.client.jar com.sap.core.js.command.client.TelnetCommandClient conf_synch
