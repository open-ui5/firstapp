package com.sap.cloud.sdk.tutorial;

//import lombok.Data;

import com.sap.cloud.sdk.result.ElementName;

//@Data
public class YY1_BL_EXTRACT_RUNType
{
    @ElementName( "SAP_UUID" )
    private String sapUuid;

    @ElementName( "RunID" )
    private String runId;

    @ElementName( "ScheduleID" )
    private String scheduleId;

    @ElementName( "ScheduleID_Text" )
    private String scheduleIdText;

    @ElementName( "TemplateID" )
    private String templateId;

    @ElementName( "TemplateID_Text" )
    private String templateIdText;

    @ElementName( "DefinitionID" )
    private String definitionId;
    
    @ElementName( "DefinitionID_Text" )
    private String definitionIdText;
    
    @ElementName( "DefinitionTypeID" )
    private String definitionTypeId;    

    @ElementName( "DefinitionTypeID_Text" )
    private String definitionTypeIdText;    

    @ElementName( "ExecutionProgram" )
    private String executionProgram;

    @ElementName( "Status" )
    private String status;

    @ElementName( "Status_Text" )
    private String statusText;
    
    @ElementName( "FileName" )
    private String fileName;
    
    @ElementName( "Cancel_ac" )
    private String cancelAction;
    
    @ElementName( "Setnextstatus_ac" )
    private String setNextStatusAction;
      
    @ElementName( "ScheduledBy" )
    private String scheduledBy;
    
	@ElementName( "ScheduledOn" )
	private String scheduledOn;    
  
	@ElementName( "ScheduledStart" )
	private String scheduledStart;

	@ElementName( "StartTime" )
	private String startTime;

	@ElementName( "EndTime" )
	private String endTime;

	@ElementName( "JobLogMessage" )
	private String jobLogMessage;

	@ElementName( "NoUser" )
	private String noUser;

	@ElementName( "Generated" )
	private String generated;
	

	@ElementName( "IsConsistent" )
	private String isConsistent;
    
	
	public String getSapUuid() {
		return sapUuid;
	}

	
}