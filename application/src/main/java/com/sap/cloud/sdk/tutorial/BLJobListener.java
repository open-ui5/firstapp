package com.sap.cloud.sdk.tutorial;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;


public class BLJobListener implements JobListener {

	private String name;
	private JobKey key;
	
	private static final Logger logger = CloudLoggerFactory.getLogger(BLJobListener.class);

	public BLJobListener(JobKey jobkey) {
		this.name = jobkey.getGroup() + "." + jobkey.getName();
	}

	public String getName() {
		return name;
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		logger.info("jobToBeExecuted");
		String jobName = context.getJobDetail().getKey().toString();

		logger.info("Job : " + jobName + " is going to start...");
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		logger.info("jobWasExecuted");
		String jobName = context.getJobDetail().getKey().toString();
		logger.info("Job : " + jobName + " is finished...");
		if (!jobException.getMessage().equals("")) {
			logger.info("Exception thrown by: " + jobName + " Exception: " + jobException.getMessage());
		}
		
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.info("jobExecutionVetoed");
	}


}