package com.sap.cloud.sdk.tutorial;


import org.quartz.Trigger;
import org.quartz.listeners.SchedulerListenerSupport;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

public class BLSchedulerListener extends SchedulerListenerSupport {

	private static final Logger logger = CloudLoggerFactory.getLogger(SchedulerServletContextListener.class);
	

	
    @Override
    public void schedulerStarted() {
		logger.info("scheduler started");
    }

    @Override
    public void schedulerShutdown() {
		logger.info("scheduler shutdown");
    }

    @Override
    public void jobScheduled(Trigger trigger) {
		logger.info("SchedulerServletContextListener started");
    }

}