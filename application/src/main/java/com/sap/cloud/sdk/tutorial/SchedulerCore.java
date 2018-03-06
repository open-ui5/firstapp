package com.sap.cloud.sdk.tutorial;

import org.quartz.JobDetail;

//import java.io.IOException;

//import org.quartz.JobDetail;
//import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
//import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


import com.google.gson.Gson;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateRequestBuilder;
import com.sap.cloud.sdk.odatav2.connectivity.ODataType;
import com.sap.cloud.sdk.odatav2.connectivity.ODataProperty;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateRequest;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateResult;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.Calendar;

// SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.NameValuePair;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import com.google.common.base.Strings;
import org.joda.time.LocalDateTime;
import java.text.ParseException;

import java.io.IOException;

@WebServlet("/schedulerCore")
public class SchedulerCore extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Scheduler BLscheduler = null;
	private static S4HANAJobDB s4HanaJobDb = null;
	private static boolean isAvailable = false;
	private static final Logger logger = CloudLoggerFactory.getLogger(SchedulerCore.class);

	protected void SchedulerCore() {

	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Check status of scheduler");
		String action = request.getParameter("action");
		if  (!Strings.isNullOrEmpty(action)){
			switch (action) {
			case "UpdateFromSAP":
				try{ 
					S4HANAJobDB.updateJobsAfterTriggerFromSAP(request);
				} catch (Exception e) {
				logger.error("- Error during scheduling SAP jobs after trigger from S4HPC", e);
				// REI >>>>>>>>>>>>>>>>>>>>>
				// Graag geen 200 terug maar 401
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Error during action " + action + ". " + e);
				return;
				}	
				
				
				response.getWriter().write("Action " + action + " is executed");		
				break;
			default:
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Action " + action + " is not supported");		
			}
			return;
		} 
		response.getWriter().write("Scheduler is available " + String.valueOf(isAvailable));
	}

	protected void initialize(SchedulerFactory sf) throws SchedulerException {
		logger.info("- initialize BlackLine Scheduler");
		if (BLscheduler == null) {
			BLscheduler = sf.getScheduler();
			isAvailable = true;
			addEventListeners();
		}
	}

	protected static Scheduler getScheduler() {
		return BLscheduler;
	}

	protected void addEventListeners() throws SchedulerException {
		if (BLscheduler != null) {
			logger.info("- add BlackLine Scheduler Listener");
			BLscheduler.getListenerManager().addSchedulerListener(new BLSchedulerListener());
		}
//		// ensure that the correct HystrixConcurrencyStrategy is used
//		new ScpNeoHystrixBootstrapListener().bootstrap();
	}

	public void run(SchedulerFactory sf) {
		try {

			initialize(sf);
			isAvailable = true;
			BLscheduler.start();
			logger.info("SCHEDULER: running");
		} catch (SchedulerException se) {
			isAvailable = false;
			BLscheduler = null;
			logger.error("SCHEDULER: not running", se);
		}
	}

	public void stop() {
		S4HANAJobDB.resetJobsInSAPAfterShutdown();
	}


	public static void scheduleJob(YY1_BL_EXTRACT_RUNType sapJobRun) {
		JobDetail job = null;
		String newStatus = null;
		LocalDateTime scheduledStart = null;
		try {
			scheduledStart = sapJobRun.getScheduledStartAsDate();
			if (scheduledStart == null) {
				// REI >>>>>>>>>>>>>>>>>>>>>
				// Hier updaten dat starttijd onbekend is
				
				return;
			}
		} catch (ParseException pe) {
			// REI >>>>>>>>>>>>>>>>>>>>>
			// Hier updaten dat starttijd geen valide datum is ?
			
		}
		logger.info("ScheduledStart: " + scheduledStart.toString());
		
		Date startdate 				= scheduledStart.toDate();
		String runId 				= sapJobRun.getRunId();		
		String scheduleId 			= sapJobRun.getScheduleId();
		String templateId  			= sapJobRun.getTemplateId();
		String definitionId  		= sapJobRun.getDefinitionId();
		String definitionTypeId  	= sapJobRun.getDefinitionTypeId();
		String executionProgram 	= sapJobRun.getExecutionProgram();
		
		String jobId				= definitionTypeId + "-" + definitionId + "-" + templateId + "-" + scheduleId + "-" + runId;
		String triggerGroupName		= definitionTypeId + "-" + definitionId + "-" + templateId + "-" + scheduleId;
		
		logger.info("- Start scheduling job " + jobId);

		// =================================================================================================================================================
		// Check if job start is in the future. Else status to ERROR: scheduled time in
		// the past
		// =================================================================================================================================================
		Date now = new Date();
		if(startdate.before(now)){
			try {
				logger.info("- Start date " + startdate.toString() + " for RunID " + runId + "before current date " + now.toString());
				newStatus = S4HANAJobDB.STATUS_ERROR;
				sapJobRun.setJobLogMessage("Startdate " + startdate.toString() + " before current date" + now.toString());
				boolean updateSuccess = S4HANAJobDB.updateSapRunJobStatus(sapJobRun, newStatus);
				if (!updateSuccess) {
					throw new Exception("Cannot update status of job in S4HPC");
				}
				return;
			} catch (Exception e) {
				logger.error("- Error during update SAP status of RunID " + runId + " to" + newStatus, e);
			}			
			return;		
		}
		
		// =================================================================================================================================================
		// Check if Execution program exists. Else status to ERROR: Execution program is
		// not supported
		// Create job. If exception then status to ERROR: creating job + exception
		// =================================================================================================================================================

		if  (Strings.isNullOrEmpty(executionProgram)){
			executionProgram = "";
		}
		
		switch (executionProgram.trim().replaceAll("\\s+", " ")) {

		case "ExchangeRates":
			job = newJob(S4hpcBlcFtpExchangeRates.class).withIdentity(jobId, executionProgram).build();
			logger.info("- " + executionProgram + " job created with id:" + jobId);
			break;

		case "GeneralLedgerBalances":
			job = newJob(S4hpcBlcFtpGeneralLedgerBalances.class).withIdentity(jobId, executionProgram).build();
			logger.info("- " + executionProgram + " job created with id:" + jobId);
			break;			
			
			
		default:
			try {
				newStatus = S4HANAJobDB.STATUS_ERROR;
				sapJobRun.setJobLogMessage("Execution Program " + executionProgram + " for job " + jobId + " does not exists.");
				boolean updateSuccess = S4HANAJobDB.updateSapRunJobStatus(sapJobRun, newStatus);
				if (!updateSuccess) {
					throw new Exception("Cannot update status of job in S4HPC");
				}
			} catch (Exception e) {
				logger.error("- Error during update SAP status of RunID " + runId + " to" + newStatus, e);
			}			
			return;
		}

		// =================================================================================================================================================
		// Create trigger. If exception then status to ERROR: creating trigger +
		// exception
		// =================================================================================================================================================	
		   Trigger trigger = newTrigger().withIdentity(runId, triggerGroupName).startAt(startdate).build();
		   if (trigger == null) {
				try {
					newStatus = S4HANAJobDB.STATUS_ERROR;
					sapJobRun.setJobLogMessage("Trigger for job " + jobId + " cannot be created.");
					boolean updateSuccess = S4HANAJobDB.updateSapRunJobStatus(sapJobRun, newStatus);
					if (!updateSuccess) {
						throw new Exception("Cannot update status of job in S4HPC");
					}
				} catch (Exception e) {
					logger.error("- Error during update SAP status of RunID " + runId + " to" + newStatus, e);
				}			
				return;			   
		   }

		// =================================================================================================================================================
		// Add job scheduler. If exception then status to ERROR: scheduling job listener
		// + exception
		// =================================================================================================================================================

		// try {
		// getScheduler().getListenerManager().addJobListener(new
		// BLJobListener(job.getKey()),
		// KeyMatcher.keyEquals(job.getKey()));
		// } catch (SchedulerException e) {
		//
		// e.printStackTrace();
		// }

		// =================================================================================================================================================
		// Schedule job. If exception then status to ERROR: scheduling job + exception
		// =================================================================================================================================================

		//
		try {
			// getScheduler().scheduleJob(job, trigger);
			newStatus = S4HANAJobDB.STATUS_SCHEDULED;
			boolean updateSuccess = S4HANAJobDB.updateSapRunJobStatus(sapJobRun, newStatus);

			// } catch (SchedulerException e) {
			//
			// e.printStackTrace();
		} catch (Exception e) {
			logger.error("- Error during update SAP status to" + newStatus, e);
		}

		logger.info("- Schedulde RunID: " + sapJobRun.getRunId() + ", Status From: " + sapJobRun.getStatus() + " to:"
				+ newStatus);

	}

	public static void cancelJob(YY1_BL_EXTRACT_RUNType sapJobRun) {
		String newStatus = sapJobRun.getStatus();
		try {

			// =================================================================================================================================================
			// Check if job is running. If this is the case, set SAP status to
			// STATUS_RUNNING_NOT_CANCELLED
			// =================================================================================================================================================
			newStatus = S4HANAJobDB.STATUS_RUNNING_NOT_CANCELLED;

			// =================================================================================================================================================
			// Check if job is scheduled, but not running. If this is the case, remove
			// scheduled job and set SAP status to STATUS_CANCELLED
			// =================================================================================================================================================
			newStatus = S4HANAJobDB.STATUS_CANCELLED;

			// =================================================================================================================================================
			// Check if job is not scheduled. If this is the case set SAP status to
			// STATUS_CANCELLED
			// =================================================================================================================================================
			newStatus = S4HANAJobDB.STATUS_CANCELLED;

			boolean updateSuccess = S4HANAJobDB.updateSapRunJobStatus(sapJobRun, newStatus);
		} catch (Exception e) {
			logger.error("- Error during update SAP status to" + newStatus, e);
		}
		logger.info("- Update RunID: " + sapJobRun.getRunId() + ", Status From: " + sapJobRun.getStatus() + " to:"
				+ newStatus);
	}

	public static void updateJob(YY1_BL_EXTRACT_RUNType sapJobRun) {
		switch (sapJobRun.getStatus()) {
		case S4HANAJobDB.STATUS_SCHEDULED:
			scheduleJob(sapJobRun);
			break;
		case S4HANAJobDB.STATUS_CANCELLING:
			cancelJob(sapJobRun);
			break;
		default:
			return;
		}
	}

}