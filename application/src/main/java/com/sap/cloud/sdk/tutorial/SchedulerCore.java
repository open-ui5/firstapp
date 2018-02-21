package com.sap.cloud.sdk.tutorial;

import org.slf4j.Logger;
import java.io.IOException;

import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
// import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import javax.servlet.http.HttpServlet;

public class SchedulerCore extends HttpServlet 
{
	
	private Scheduler scheduler = null;
	
	protected void SchedulerCore() throws Exception {
		SchedulerFactory sf = new StdSchedulerFactory();
		scheduler = sf.getScheduler();
	}
	
	
	
	public void run()   {

	 
    	// Start scheduler
    	
    	// Lees alle jobs uit SAP met niet de status hebben: STOPPED BY USER OF READY
    	// Voor job met status INITIAL
    	// - indien gestart in SAP updaten naar RUNNING
    	// - indien niet aanwezig, inplannen en SAP updaten naar PLANNED
    	// - indien status gepland dan SAP updaten naar RUNNING
    	// Voor job met status PLANNED
    	// - indien status running dan SAP updaten naar RUNNING
    	// - indien niet gestart, dan als de starttijd + threshold ligt voor de huidige tijd en SAP updaten naar DELAYED
    	// - indien niet gestart, dan als de starttijd + threshold ligt na de huidige tijd dan ok
    	// - indien gestart, SAP updaten naar RUNNING
    	// - indien afgelopen, dan naar FINSIHED
    	// Voor job met status DELAYED
       	// - indien status vertraagd dan SAP updaten naar RUNNING

    	// Voor job met status CANCEL
    	// - indien gestart en status afbreken, dan job afbreken, status in SAP updaten naar afgebroken op verzoek gebruiker
    	// - indien gepland en status afbreken, dan job afbreken, status in SAP updaten naar afgebroken op verzoek gebruiker
    	// Voor job met status INITIAL
    	// Check
    	// 
    		
	}
	
	public void stop()  {
	
	}
	
	public void createJobs() {
		// Here we need to read definition catalog of SAP and for every definition we have to schedule a Quatz Job (without a trigger)
		// The 

	}
	
	public void scheduleJobMonitor()  {
		// In this we need 
		
		
//		scheduler.getListenerManager().addSchedulerListener(mySchedListener);
//		scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
//		scheduler.getListenerManager().addTriggerListener(myTriggerListener, allTriggers());
		

	}
}