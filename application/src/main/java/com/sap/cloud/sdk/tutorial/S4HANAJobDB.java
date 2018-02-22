package com.sap.cloud.sdk.tutorial;

import com.google.gson.Gson;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateRequestBuilder;
import com.sap.cloud.sdk.odatav2.connectivity.ODataType;
import com.sap.cloud.sdk.odatav2.connectivity.ODataProperty;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

public class S4HANAJobDB  {
	
    private static final String STATUS_RUNNING = "03";
    private static final String STATUS_CRASHED = "05";

	public static void checkJobsInSAPAfterShutdown() {
	 try {
     	
		 
//      1. Read all jobs with status RUNNING.		 
		 
     	String destinationName = "S4HPC_EXTERNAL";
     	ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
     	final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);
     	final List<YY1_BL_EXTRACT_RUNType> sapJobRunList = ODataQueryBuilder
     	        .withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds",
     	                "YY1_BL_EXTRACT_RUN")
     	        .select("SAP_UUID",
     	                "RunID",
     	                "Status")
     	        .filter(ODataProperty.field("Status").eq(ODataType.of(STATUS_RUNNING)))
     	        .build()
     	        .execute(endpoint)
     	        .asList(YY1_BL_EXTRACT_RUNType.class);
     	
 //     2. If list exist, we need a CSRF token
//     	final String destinationUriString = DestinationAccessor.getDestination("ErpQueryEndpoint").getUri().toString();
//     	final HttpClient httpClient = HttpClientAccessor.getHttpClient("ErpQueryEndpoint");
//     	final URI uri = new URI(destinationUriString + "/sap/opu/odata/sap/NAME_OF_THE_API/$metadata");
//     	final HttpResponse httpResponse = httpClient.execute(new HttpGet(uri));
     	
     	
 //     2. When jobs exists with status RUNNING, set them to CRASHED and replan job     	
     	
     	for (YY1_BL_EXTRACT_RUNType sapJobRun : sapJobRunList) {

     		Map<String,Object> keys = new HashMap<String, Object>();
     		Map<String,Object> entityData = new HashMap<String, Object>();
     		
     		
     		// Fill the key field for the updated record 
     		keys.put("SAP_UUID",sapJobRun.getSapUuid());
     		
     		// Fill the field to be updated
     		keys.put("Status",STATUS_CRASHED);
     		
     		
     		
     		int httpStatusCode = ODataUpdateRequestBuilder
     				.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds",
     							"YY1_BL_EXTRACT_RUN", keys)
//     				.withHeader(String key, String value)
     				.withBody(entityData)
     				.build()
         	        .execute(endpoint)
         	        .getHttpStatusCode();
			
			
			
			
		}




     } catch (final ODataException e) {

     }
	
	}
	
}