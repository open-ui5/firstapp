package com.sap.cloud.sdk.tutorial;

//import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;


@WebListener
public class SchedulerServletContextListener
               implements ServletContextListener{
	
	private SchedulerCore scheduler = new SchedulerCore();

	private static final Logger logger = CloudLoggerFactory.getLogger(SchedulerServletContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("SchedulerServletContextListener destroyed");
		scheduler.stop();
	}

    //Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.info("SchedulerServletContextListener started");
		scheduler.run();
	}
}