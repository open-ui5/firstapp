package com.sap.cloud.sdk.tutorial;

//import lombok.Data;

import com.sap.cloud.sdk.result.ElementName;

//@Data
public class GeneralLedgerBalancesType
{
    @ElementName( "Ledger" )
    private String ledger;

    @ElementName( "CompanyCode" )
    private String companyCode;

    @ElementName( "LedgerFiscalYear" )
    private String ledgerFiscalYear;

    @ElementName( "LedgerFiscalPeriod" )
    private String ledgerFiscalPeriod;

    @ElementName( "IsNotPostedTo" )
    private String isNotPostedTo;
    
    @ElementName( "CreditAmountInCoCodeCrcy" )
    private String creditAmountInCoCodeCrcy;
    
    @ElementName( "DebitAmountInCompanyCodeCrcy" )
    private String debitAmountInCompanyCodeCrcy;
    
    @ElementName( "BalAmtInCompanyCodeCrcy" )
    private String balAmtInCompanyCodeCrcy;
    
    @ElementName( "AccmltdBalAmtInCoCodeCrcy" )
    private String accmltdBalAmtInCoCodeCrcy;
  
    @ElementName( "CompanyCodeCurrency" )
    private String companyCodeCurrency;
  
    @ElementName( "CreditAmountInGlobalCrcy" )
    private String creditAmountInGlobalCrcy;
  
    @ElementName( "DebitAmountInGlobalCrcy" )
    private String debitAmountInGlobalCrcy;
  
    @ElementName( "BalanceAmountInGlobalCrcy" )
    private String balanceAmountInGlobalCrcy;
  
    @ElementName( "AccumulatedBalAmtInGlobalCrcy" )
    private String accumulatedBalAmtInGlobalCrcy;
  
    @ElementName( "GlobalCurrency" )
    private String globalCurrency;    
    
}

