package com.sap.cloud.sdk.tutorial;


//import java.io.IOException;


//import org.quartz.JobDetail;
//import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
//import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;



// SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/schedulerCore")
public class SchedulerCore extends HttpServlet {

	private static Scheduler BLscheduler = null;
	private static boolean isAvailable = false;
    private static final long serialVersionUID = 1L;

	private static final Logger logger = CloudLoggerFactory.getLogger(SchedulerCore.class);

	
	protected void SchedulerCore() {

	}
	
	
    @Override
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
        throws ServletException, IOException
    {
        logger.info("Check status of scheduler");
        response.getWriter().write("Scheduler is available "+ String.valueOf(isAvailable));
    }

	protected void initialize(SchedulerFactory sf) throws SchedulerException {
		logger.info("initialize Scheduler");
		if (BLscheduler == null) {
			BLscheduler = sf.getScheduler();
			isAvailable = true;
			addEventListeners();
		}
	}

	protected void addEventListeners() throws SchedulerException {
		if (BLscheduler != null) {
			logger.info("add BlackLine Scheduler Listener");
			BLscheduler.getListenerManager().addSchedulerListener(new BLSchedulerListener());
		}
	}

	public void run(SchedulerFactory sf) {

		// Start the scheduler
		// Re
		try {
			initialize(sf);
			isAvailable = true;
			logger.info("Scheduler is running");
			
			
			
			

		} catch (SchedulerException se) {
			isAvailable = false;
			logger.error("Scheduler is not running", se);
		}

		// Start scheduler

		// Lees alle jobs uit SAP met niet de status hebben: STOPPED BY USER OF READY
		// Voor job met status INITIAL
		// - indien gestart in SAP updaten naar RUNNING
		// - indien niet aanwezig, inplannen en SAP updaten naar PLANNED
		// - indien status gepland dan SAP updaten naar RUNNING
		// Voor job met status PLANNED
		// - indien status running dan SAP updaten naar RUNNING
		// - indien niet gestart, dan als de starttijd + threshold ligt voor de huidige
		// tijd en SAP updaten naar DELAYED
		// - indien niet gestart, dan als de starttijd + threshold ligt na de huidige
		// tijd dan ok
		// - indien gestart, SAP updaten naar RUNNING
		// - indien afgelopen, dan naar FINSIHED
		// Voor job met status DELAYED
		// - indien status vertraagd dan SAP updaten naar RUNNING

		// Voor job met status CANCEL
		// - indien gestart en status afbreken, dan job afbreken, status in SAP updaten
		// naar afgebroken op verzoek gebruiker
		// - indien gepland en status afbreken, dan job afbreken, status in SAP updaten
		// naar afgebroken op verzoek gebruiker
		// Voor job met status INITIAL
		// Check
		//

	}
	


	public void stop() {

	}

	public void checkJobsInSAPAfterShutdown() {
		// Here we have to check the actual status in SAP. If there are running jobs, this is meaning something did go wrong when stopped the last time.
		// We have to update the status and plan a new run.

	}

	public void scheduleJobMonitor() {
		// In this we need

		// scheduler.getListenerManager().addSchedulerListener(mySchedListener);
		// scheduler.getListenerManager().addJobListener(myJobListener, allJobs());
		// scheduler.getListenerManager().addTriggerListener(myTriggerListener,
		// allTriggers());

	}
}