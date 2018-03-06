package com.sap.cloud.sdk.tutorial;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.Trigger.CompletedExecutionInstruction;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

import java.util.Date;

public class BLTriggerListener implements TriggerListener {

	private static final String TRIGGER_LISTENER_NAME = "BLTriggerListener";
	private static final Logger logger = CloudLoggerFactory.getLogger(BLTriggerListener.class);
	
	public BLTriggerListener() {
	}

	public String getName() {
		return TRIGGER_LISTENER_NAME;
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
		logger.info("BLTriggerListener->triggerComplete");	
		String jobName = context.getJobDetail().getKey().toString();
		Date actualFireTime = context.getFireTime();
		long jobRunTime = getJobRunTime();
		
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		logger.info("BLTriggerListener->triggerFired");
	}

	public void triggerMisfired(Trigger trigger) {
		logger.info("BLTriggerListener->triggerMisfired");
	}

	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		logger.info("BLTriggerListener->vetoJobExecution");
		return false;
	}


	
}