This folder contains scripts and properties files for synching the central configuration

File config.properties contains information about the location of the central configuration
and which subfolder of <central configuration>/ljs/configuration is relevant for the current node

The main script is synch_config.sh
First it makes a backup of the current config_master
After that it copies config_master from the central configuration and in files go.sh(.bat)
substites ports and parameters as defined in the central configuration in 
<central_configuration>/ljs/template/nodes/node[i]/node.properties

The configuration of the script is stored in "config_master" according to the concept of the central configuration. Parameters are located in a file under:
<LJS-HOME>/config_master/com.sap.ljs.commandline.parameters/ljs.properties

