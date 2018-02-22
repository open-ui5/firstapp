package com.sap.cloud.sdk.tutorial;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

// SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;


@WebListener
public class SchedulerServletContextListener
	extends  QuartzInitializerListener{
	
	private SchedulerCore scheduler = new SchedulerCore();

	private static final Logger logger = CloudLoggerFactory.getLogger(SchedulerServletContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("SchedulerServletContextListener destroyed");
		scheduler.stop();
	}

    //Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("SchedulerServletContextListener started");	
		super.contextInitialized(sce);
		ServletContext ctx = sce.getServletContext();
		StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QUARTZ_FACTORY_KEY);
		scheduler.run(factory);
	}
}