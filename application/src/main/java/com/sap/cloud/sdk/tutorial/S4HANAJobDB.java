package com.sap.cloud.sdk.tutorial;

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
import com.sap.cloud.sdk.odatav2.connectivity.ODataQuery;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryResult;
import com.sap.cloud.sdk.cloudplatform.servlet.RequestContextExecutor;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationsRequestContextListener;
import com.sap.cloud.sdk.cloudplatform.servlet.DefaultRequestContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpNeoDestinationsRequestContextListener;
import com.sap.cloud.sdk.frameworks.hystrix.ScpNeoHystrixBootstrapListener;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantRequestContextListener;
import com.sap.cloud.sdk.cloudplatform.security.user.UserRequestContextListener;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.frameworks.hystrix.ScpNeoHystrixBootstrapListener;
import com.sap.cloud.sdk.frameworks.hystrix.HystrixRequestContextFacade;
import com.sap.cloud.sdk.cloudplatform.servlet.DefaultRequestContextFactory;
import com.sap.cloud.sdk.cloudplatform.servlet.RequestContext;
import javax.servlet.http.HttpServletRequest;

public class S4HANAJobDB {

	public static final String STATUS_PLANNED 				= "01";
	public static final String STATUS_SCHEDULED 			= "02";
	public static final String STATUS_RUNNING 				= "03";
	public static final String STATUS_FINSHED 				= "04";
	public static final String STATUS_CANCELLING 			= "05";
	public static final String STATUS_CANCELLED		 		= "06";
	public static final String STATUS_ERROR 				= "07";
	public static final String STATUS_CRASHED				= "08";
	public static final String STATUS_REPLAN				= "09";
	public static final String STATUS_RUNNING_NOT_CANCELLED = "10";
	
	private static final Logger logger = CloudLoggerFactory.getLogger(S4HANAJobDB.class);
	private static final String destinationName = "S4HPC_EXTERNAL";
	private static ErpConfigContext erpConfigContext = null;
	private static ErpEndpoint endpoint = null;
	private static RequestContextExecutor requestContextExecutor = null;
	
	public static void initS4HANAJobDB(HttpServletRequest request) {
		try {
			if (erpConfigContext == null) {
				erpConfigContext = new ErpConfigContext(destinationName);
			}
			if (endpoint == null) {
				endpoint = new ErpEndpoint(erpConfigContext);
			}
			if (requestContextExecutor == null) {

				// ensure that the correct HystrixConcurrencyStrategy is used
				new ScpNeoHystrixBootstrapListener().bootstrap();	

				requestContextExecutor = new RequestContextExecutor()
						.withListeners(	new DestinationsRequestContextListener(),
										new ScpNeoDestinationsRequestContextListener(), 
										new TenantRequestContextListener(),
										new UserRequestContextListener());
				if (request != null) {
					
					//RequestContext requestContext = 
					//		new DefaultRequestContextFactory().newRequestContext(request);
					//new HystrixRequestContextFacade().setCurrentContext(new DefaultRequestContext());
					//requestContextExecutor = requestContextExecutor.withRequestContext(requestContext);
				}
			}
		} catch (final Exception e) {
			logger.error("- Error during reset crashed SAP statusses", e);
		}
	}
	
	public static void initS4HANAJobDB() {
		initS4HANAJobDB(null);
	}
	
	public static void resetSAPStatussesAfterCrash() {
		try {
			logger.info("- Check SAP statussen after crash");
			// 1. Read all jobs with status RUNNING.
//			ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
//			final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);
//			ODataQueryResult queryResult = new RequestContextExecutor()
//					.withListeners(new DestinationsRequestContextListener(),
//							new ScpNeoDestinationsRequestContextListener(), new TenantRequestContextListener(),
//							new UserRequestContextListener())
			initS4HANAJobDB();
			ODataQueryResult queryResult = requestContextExecutor
					.execute(new Callable<ODataQueryResult>() {
						@Override
						public ODataQueryResult call() throws ODataException {

							ODataQuery queryRequest = ODataQueryBuilder
									.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds", "YY1_BL_EXTRACT_RUN")
									.select("SAP_UUID", "RunID", "Status")
									.filter(ODataProperty.field("Status").eq(ODataType.of(STATUS_RUNNING))
											.or(ODataProperty.field("Status").eq(ODataType.of(STATUS_SCHEDULED)))
											.or(ODataProperty.field("Status").eq(ODataType.of(STATUS_CANCELLING))))
									.build();

							return queryRequest.execute(endpoint);
						}
					});
			final List<YY1_BL_EXTRACT_RUNType> sapJobRunList = queryResult.asList(YY1_BL_EXTRACT_RUNType.class);

			if (sapJobRunList.size() > 0) {
				logger.info("- SAP Jobs during crash found");
			}

			// 2. When jobs exists with status RUNNING, set them to CRASHED and replan job
			for (YY1_BL_EXTRACT_RUNType sapJobRun : sapJobRunList) {
				String newStatus = null;
				switch (sapJobRun.getStatus()) {
				case STATUS_RUNNING:
					newStatus = STATUS_CRASHED;
					break;
				case STATUS_SCHEDULED:
					newStatus = STATUS_REPLAN;
					break;
				case STATUS_CANCELLING:
					newStatus = STATUS_CANCELLED;
					break;
				default:
					continue;
				}

				boolean updateSuccess = updateSapRunJobStatus(sapJobRun, newStatus);

				logger.info("- Change RunID: " + sapJobRun.getRunId() + ", Status From: " + sapJobRun.getStatus()
						+ " to: " + newStatus);

			}
			logger.info("- Finished check SAP statusses after crash");
		} catch (final Exception e) {
			logger.error("- Error during reset crashed SAP statusses", e);
		}
	}

	public static boolean updateSapRunJobStatus(YY1_BL_EXTRACT_RUNType sapJobRun, String newStatus)
			throws ODataException, Exception {
//		ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
//		final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);

		final Map<String, Object> keys = new HashMap<String, Object>();
		final Map<String, Object> entityData = new HashMap<String, Object>();

		// Fill the key field for the updated record
		keys.put("SAP_UUID", sapJobRun.getSapUuid());

		entityData.put("RunID", sapJobRun.getRunId());
		entityData.put("Status", newStatus);
		entityData.put("NoUser", true);
		if  (!Strings.isNullOrEmpty(sapJobRun.getJobLogMessage())){
			entityData.put("JobLogMessage", sapJobRun.getJobLogMessage());
		}
		
		

		// We should add some retry logic when enry is locked.

//		ODataUpdateResult updateResult = new RequestContextExecutor()
//				.withListeners(new DestinationsRequestContextListener(), new ScpNeoDestinationsRequestContextListener(),
//						new TenantRequestContextListener(), new UserRequestContextListener())
		initS4HANAJobDB();
		ODataUpdateResult updateResult = requestContextExecutor
				.execute(new Callable<ODataUpdateResult>() {
					@Override
					public ODataUpdateResult call() throws ODataException {

						ODataUpdateRequest updateRequest = ODataUpdateRequestBuilder
								.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds", "YY1_BL_EXTRACT_RUN", keys)
								.withBodyAsMap(entityData).build();
						return updateRequest.execute(endpoint);
					}
				});

		return true;

	}

	public static void schedulingJobsAfterStart() {	
		//=================================================================================================================================================
		// This method is called when the scheduler is started and will schedule the jobs 
		// Planned -> schedule job -> Scheduled
		// Replan -> schedule job -> Scheduled
		//=================================================================================================================================================
		
		try {
			logger.info("- Start scheduling SAP Jobs");

//			ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
//			final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);

//			ODataQueryResult queryResult = new RequestContextExecutor()
//					.withListeners(new DestinationsRequestContextListener(),
//							new ScpNeoDestinationsRequestContextListener(), new TenantRequestContextListener(),
//							new UserRequestContextListener())
			initS4HANAJobDB();
			ODataQueryResult queryResult = requestContextExecutor
					.execute(new Callable<ODataQueryResult>() {
						@Override
						public ODataQueryResult call() throws ODataException {

							ODataQuery queryRequest = ODataQueryBuilder
									.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds", "YY1_BL_EXTRACT_RUN")
									.select("SAP_UUID", "RunID", "ScheduleID", "TemplateID", "DefinitionID", "DefinitionTypeID", "Status", "ScheduledStart")
									.filter(ODataProperty.field("Status").eq(ODataType.of(STATUS_PLANNED))
											.or(ODataProperty.field("Status").eq(ODataType.of(STATUS_REPLAN))))
									.build();

							return queryRequest.execute(endpoint);
						}
					});

			final List<YY1_BL_EXTRACT_RUNType> sapJobRunList = queryResult.asList(YY1_BL_EXTRACT_RUNType.class);

			if (sapJobRunList.size() > 0) {
				logger.info("- SAP Jobs found for scheduling");
			}
			for (YY1_BL_EXTRACT_RUNType sapJobRun : sapJobRunList) {
				try {
					SchedulerCore.scheduleJob(sapJobRun);
				} catch (Exception e) {
					logger.error("- Error during scheduling SAP jobs", e);
				}
			}

			logger.info("- Finish scheduling SAP Jobs");
		} catch (Exception e) {
			logger.error("- Error during scheduling SAP jobs", e);
		}
	}
	
	
	public static void updateJobsAfterTriggerFromSAP(HttpServletRequest request) throws Exception {
		//=================================================================================================================================================
		// This method is triggered by SA4HPC after chaning the SapJobRun list when the scheduler is already running to update the job 
		// Planned -> schedule job -> Scheduled
		// Cancelling -> when job scheduled,delete job -> Cancelled
		// Cancelling -> when job running -> Running_not_cancelled
		//=================================================================================================================================================

//		try {
			logger.info("- Update scheduling SAP Jobs after trigger from S4HPC");

//			ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
//			final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);
		

//			ODataQueryResult queryResult = new RequestContextExecutor()
//					.withListeners(new DestinationsRequestContextListener(),
//							new ScpNeoDestinationsRequestContextListener(), new TenantRequestContextListener(),
//							new UserRequestContextListener())
			initS4HANAJobDB(request);
			ODataQueryResult queryResult = requestContextExecutor

					.execute(new Callable<ODataQueryResult>() {
						@Override
						public ODataQueryResult call() throws ODataException {

							ODataQuery queryRequest = ODataQueryBuilder
									.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds", "YY1_BL_EXTRACT_RUN")
									.select("SAP_UUID", "RunID", "ScheduleID", "TemplateID", "DefinitionID", "DefinitionTypeID", "Status", "ScheduledStart")
									.filter(ODataProperty.field("Status").eq(ODataType.of(STATUS_PLANNED))
											.or(ODataProperty.field("Status").eq(ODataType.of(STATUS_CANCELLING))))
									.build();

							return queryRequest.execute(endpoint);
						}
					});

			final List<YY1_BL_EXTRACT_RUNType> sapJobRunList = queryResult.asList(YY1_BL_EXTRACT_RUNType.class);

			if (sapJobRunList.size() > 0) {
				logger.info("- SAP Jobs found for update");
			}
			for (YY1_BL_EXTRACT_RUNType sapJobRun : sapJobRunList) {
				try {
					SchedulerCore.updateJob(sapJobRun);
				} catch (Exception e) {
					logger.error("- Error during update SAP jobs after S4HPC trigger", e);
				}
			}

			logger.info("- Finish scheduling SAP Jobs after trigger from S4HPC");
//		} catch (Exception e) {
//			logger.error("- Error during scheduling SAP jobs after trigger from S4HPC", e);
//		}		

	}
	
	public static void resetJobsInSAPAfterShutdown() {
		// Here we have to check the actual status in SAP. If there are running jobs,
		// this is meaning something did go wrong when stopped the last time.
		// We have to update the status and plan a new run.

	}
	
}