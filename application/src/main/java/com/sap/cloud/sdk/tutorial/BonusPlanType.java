package com.sap.cloud.sdk.tutorial;

//import lombok.Data;

import com.sap.cloud.sdk.result.ElementName;

//@Data
public class BonusPlanType
{
    @ElementName( "SAP_UUID" )
    private String sapUuid;

    @ElementName( "ID" )
    private String id;

    @ElementName( "ValidityStartdate" )
    private String validityStartdate;

    @ElementName( "ValidityEnddate" )
    private String validityEnddate;

    @ElementName( "HighBonusAssignmentFactor" )
    private String highBonusAssignmentFactor;
}