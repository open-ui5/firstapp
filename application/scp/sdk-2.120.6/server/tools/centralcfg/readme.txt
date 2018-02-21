This folder contains several templates for configuring several LJS nodes on one host.
It usually resides on a "central" place in the landscape (network share) and is copied by the LJS "worker nodes".
It can be extended and used to create LJS configuration for landscape.
It should be shared with read/write permissions and its location should be pointed to each LJS node in file <LJS_Home>\tools\syncconfig\config.properties

\central_configuration\templates - contains templates for the configuration of the LJS nodes. Each LJS node belongs to one the templates in the central configuration share
	In the case of e-sourcing there are four templates
	ESO - e-sourcing application
	ESO_JMS - e-sourcing application and JMS is activated
	ESO_JMS_OPT - e-sourcing application, optimizer application and JMS is activated
	JMS_OPT - optimizer application and JMS is activated

\central_configuration\templates\templateXXX\config_master\inactive - contains a sample of the main configuration for the LJS node under <LJS_HOME>/config_master
	and is used as a template by each LJS node when it synchs the configuration from the central share
\central_configuration\templates\templateXXX\config_master\active - contains a sample of the main configuration for the LJS node under <LJS_HOME>/config_master
	and it is used for mass configuration by the Admin node
\central_configuration\templates\templateXXX\nodes\nodeY.properties - property file with specific information about the LJS node nodeY - unique ports, IDs, names etc.
\central_configuration\templates\templateXXX\bundles.info - contains a list of all relevant bundles for the LJS node and if they they should be started by default. On the LJS node file bundles.info 
	is copied under <LJS_HOME>\configuration\org.eclipse.equinox.simpleconfigurator

\central_configuration\apps - contains .war files some of which are deployed on the LJS node. The exact .war files for each template are described in 
	\central_configuration\templates\templateXXX\template.properties

\central_configuration\apps_configuration - contains configuration files necessary for any of the applications in \central_configuration\apps
	In the case of e-sourcing it should contain the e-sourcing home folder under FCI_Home which is used by fcioptimizer.war and fcsourcing.war

\central_configuration\tools_configuration - contains configuration for external tools like nagios and splunk
\central_configuration\tools_configuration\splunk - contains property file monitoring_resources.list which describes the log files that should be monitored in Splunk It is used when configuring Log Monitoring with Splunk
\central_configuration\tools_configuration\nagios - contains property file config_type.properties which is used when executing script /tools/configurationfiles/create_properties_file.sh(bat) on the LJS nodes and later on the information in 
	config_type.properties is used when configuring nagios

\central_configuration\distribution - contains .zip file with the relevant LJS distribution <distribution_name>.zip. After that when the LJS nodes are updated the <LJS_HOME>/plugins folder is updated according to /plugins folder in <distribution_name>.zip
\central_configuration\landscape_directory - contains one property file <host>_<jmx_port>_<datetime>.properties for each LJS node in the landscape. This file is created by executing a script on the LJS itself create_properties_file.sh(bat)