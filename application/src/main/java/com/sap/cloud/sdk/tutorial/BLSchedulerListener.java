package com.sap.cloud.sdk.tutorial;


import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.listeners.SchedulerListenerSupport;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.cloudplatform.servlet.Executable;

import com.sap.cloud.sdk.cloudplatform.servlet.RequestContextExecutor;


public class BLSchedulerListener extends SchedulerListenerSupport {

	private static final Logger logger = CloudLoggerFactory.getLogger(BLSchedulerListener.class);
	

	
    @Override
    public void schedulerStarted() {
		logger.info("SCHEDULER: Started");
		S4HANAJobDB.resetSAPStatussesAfterCrash();
		S4HANAJobDB.schedulingJobsAfterStart();
    }

    @Override
    public void schedulerShutdown() {
		logger.info("SCHEDULER: Shutdown");
    }
   
    @Override
    public void jobScheduled(Trigger trigger) {
    	logger.info("------- MySchedulerListener  -> jobScheduled:Trigger->"+trigger.getKey());
    }

	@Override
	public void schedulerError(String msg, SchedulerException cause) {
    	logger.info("------- MySchedulerListener  -> schedulerError:");
		
	}

	@Override
	public void schedulerInStandbyMode() {
    	logger.info("------- MySchedulerListener  -> schedulerInStandbyMode:");
		
	}

	@Override
	public void schedulingDataCleared() {
    	logger.info("------- MySchedulerListener  -> schedulingDataCleared:");
		
	}

	@Override
	public void jobAdded(JobDetail jobDetail) {
		logger.info("------- MySchedulerListener  -> jobAdded:Job"+jobDetail.getKey());
		
	}

	@Override
	public void jobDeleted(JobKey jobKey) {
		logger.info("------- MySchedulerListener  -> jobDeleted:Job"+jobKey);
		
	}

	@Override
	public void jobPaused(JobKey jobKey) {
		logger.info("------- MySchedulerListener  -> jobPaused:Job"+jobKey);
		
	}

	@Override
	public void jobResumed(JobKey jobKey) {
		logger.info("------- MySchedulerListener  -> jobResumed:Job"+jobKey);
		
	}

	@Override
	public void jobUnscheduled(TriggerKey triggerKey) {
		logger.info("------- MySchedulerListener  -> jobUnscheduled:Trigger"+triggerKey);
		
	}

	@Override
	public void jobsPaused(String jobGroup) {
		logger.info("------- MySchedulerListener  -> jobsPaused:jobGroup"+jobGroup);
		
	}

	@Override
	public void jobsResumed(String jobGroup) {
		logger.info("------- MySchedulerListener  -> jobsResumed:jobGroup"+jobGroup);
		
	}

	@Override
	public void schedulerShuttingdown() {
		logger.info("------- MySchedulerListener  -> schedulerShuttingdown:");
		
	}

	@Override
	public void schedulerStarting() {
		logger.info("- starting up scheduler");		
	}

	@Override
	public void triggerPaused(TriggerKey triggerKey) {
		logger.info("------- MySchedulerListener  -> triggerPaused:Trigger"+triggerKey);
		
	}

	@Override
	public void triggerResumed(TriggerKey triggerKey) {
		logger.info("------- MySchedulerListener  -> triggerResumed:Trigger"+triggerKey);
		
	}

	@Override
	public void triggersPaused(String triggerGroup) {
		logger.info("------- MySchedulerListener  -> triggersPaused:triggerGroup"+triggerGroup);
		
	}

	@Override
	public void triggersResumed(String triggerGroup) {
		logger.info("------- MySchedulerListener  -> triggersResumed:triggerGroup"+triggerGroup);
		
	}

	@Override
	public void triggerFinalized(Trigger trigger) {
		logger.info("------- MySchedulerListener  -> triggerFinalizedTrigger->"+trigger.getKey());
		
	}    
    
    
    
}