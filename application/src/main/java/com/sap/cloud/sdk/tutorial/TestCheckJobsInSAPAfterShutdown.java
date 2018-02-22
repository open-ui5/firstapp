




package com.sap.cloud.sdk.tutorial;

import com.google.gson.Gson;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.odatav2.connectivity.ODataType;
import com.sap.cloud.sdk.odatav2.connectivity.ODataProperty;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateRequest;
import com.sap.cloud.sdk.odatav2.connectivity.ODataUpdateRequestBuilder;

@WebServlet("/testCheckJobsInSAPAfterShutdown")
public class TestCheckJobsInSAPAfterShutdown extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = CloudLoggerFactory.getLogger(TestCheckJobsInSAPAfterShutdown.class);

    private static final String STATUS_PLANNED    = "01";
    private static final String STATUS_SCHEDULED  = "02";
    private static final String STATUS_RUNNING    = "03";
    private static final String STATUS_FINSHED    = "04";
    private static final String STATUS_CANCELLING = "05";
    private static final String STATUS_CANCELLED  = "06";
    private static final String STATUS_ERROR      = "07";
    private static final String STATUS_CRASHED    = "08";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
        	
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
        	
        	if (sapJobRunList.size() > 0){
        		logger.info("SAP Jobs with status running found");
        	}


         	for (YY1_BL_EXTRACT_RUNType sapJobRun : sapJobRunList) {

         		Map<String,Object> keys = new HashMap<String, Object>();
         		Map<String,Object> entityData = new HashMap<String, Object>();

         		// Fill the key field for the updated record 
         		keys.put("SAP_UUID",sapJobRun.getSapUuid());
         		
         		// Fill the field to be updated
         		entityData.put("Status",STATUS_CRASHED);
    	
         		final ODataUpdateRequest updateRequest = ODataUpdateRequestBuilder
         				.withEntity("/sap/opu/odata/sap/yy1_bl_extract_run_cds",
         							"YY1_BL_EXTRACT_RUN", keys)
         				.withBodyAsMap(entityData)
         				.build();
         		
         		
         		updateRequest.execute(endpoint);
         		
             	logger.info("Set to CRASHED: " + sapJobRun.getSapUuid() + ", ");
    		}
        	

        	
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(sapJobRunList));

        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}