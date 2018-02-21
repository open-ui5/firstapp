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

import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;

@WebServlet("/bonusplan")
public class BonusPlanServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = CloudLoggerFactory.getLogger(BonusPlanServlet.class);

    private static final String CATEGORY_PERSON = "1";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
        	
        	String destinationName = "S4HPC_EXTERNAL";
        	ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
        	final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);
        	final List<BonusPlanType> bonusPlan = ODataQueryBuilder
        	        .withEntity("/sap/opu/odata/sap/YY1_BONUSPLAN_CDS",
        	                "YY1_BONUSPLAN")
        	        .select("SAP_UUID",
        	                "ID",
        	                "ValidityStartdate",
        	                "ValidityEnddate",
        	                "HighBonusAssignmentFactor")
        	        .build()
        	        .execute(endpoint)
        	        .asList(BonusPlanType.class);
        	
        	
        	
   

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(bonusPlan));

        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}