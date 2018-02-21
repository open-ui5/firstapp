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
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.odatav2.connectivity.ODataProperty;
import com.sap.cloud.sdk.odatav2.connectivity.ODataQueryBuilder;
//import com.sap.cloud.sdk.odatav2.connectivity.ComplexFilterExpression;
import com.sap.cloud.sdk.odatav2.connectivity.FilterExpression;
import com.sap.cloud.sdk.odatav2.connectivity.ODataType;


@WebServlet("/glbalances")
public class GeneralLedgerBalancesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = CloudLoggerFactory.getLogger(GeneralLedgerBalancesServlet.class);

    private static final String CATEGORY_PERSON = "1";
    public static final GeneralLedgerBalancesType GENERAL_LEDGER = new GeneralLedgerBalancesType();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
        	
//        	String destinationName = "S4HPC_EXTERNAL";

//        	ErpConfigContext erpConfigContext = new ErpConfigContext(destinationName);
//        	final ErpEndpoint endpoint = new ErpEndpoint(erpConfigContext);
        	final ErpEndpoint endpoint = new ErpEndpoint(); //local
        	
        			
        	
        	final List<GeneralLedgerBalancesType> glAccountBalances = ODataQueryBuilder
        	        .withEntity("/sap/opu/odata/sap/FAC_GL_ACCOUNT_BALANCE_SRV",
        	                "GL_ACCOUNT_BALANCESet")
        	        .select("Ledger",
        	                "CompanyCode",
        	                "LedgerFiscalYear",
        	                "LedgerFiscalPeriod",
        	                "IsNotPostedTo",
        	                "CreditAmountInCoCodeCrcy",
        	                "DebitAmountInCompanyCodeCrcy",
        	                "BalAmtInCompanyCodeCrcy",
        	                "AccmltdBalAmtInCoCodeCrcy",
        	                "CompanyCodeCurrency",
        	                "CreditAmountInGlobalCrcy",
        	                "DebitAmountInGlobalCrcy",
        	                "BalanceAmountInGlobalCrcy",
        	                "AccumulatedBalAmtInGlobalCrcy",
        	                "GlobalCurrency")
        	        .filter(ODataProperty.field("Ledger").eq(ODataType.of("0L")))
        	        .filter(ODataProperty.field("CompanyCode").eq(ODataType.of("1710")))
        	        .filter(ODataProperty.field("LedgerFiscalPeriod").eq(ODataType.of("2018")))
        	        .build()
        	        .execute(endpoint)
        	        .asList(GeneralLedgerBalancesType.class);
        	
        	
        	
   

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(glAccountBalances));

        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}