package com.sap.cloud.sdk.tutorial;

import org.quartz.StatefulJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//SAP Cloud logger
import org.slf4j.Logger;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;

//=================================================================================================================================================
// S/4HANA Public Cloud to BlackLine Cloud throught FTP extractor for General Ledger Balances  
//=================================================================================================================================================

public class S4hpcBlcFtpGeneralLedgerBalances implements StatefulJob {

	private static final Logger logger = CloudLoggerFactory.getLogger(S4hpcBlcFtpGeneralLedgerBalances.class);
	
	public S4hpcBlcFtpGeneralLedgerBalances() {
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("- Execute job S4hpcBlcFtpGeneralLedgerBalances");
	}
}

