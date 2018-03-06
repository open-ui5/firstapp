package com.sap.cloud.sdk.tutorial;

import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

import com.sap.cloud.sdk.result.ElementName;
import com.sap.cloud.sdk.result.GsonResultPrimitive;
import com.sap.cloud.sdk.s4hana.serialization.LocalDateConverter;
import com.sap.cloud.sdk.s4hana.serialization.LongConverter;
import java.util.UUID;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.LocalDateTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;

@Getter
@ToString
public class YY1_BL_EXTRACT_RUNType {
	@ElementName("SAP_UUID" )
    private UUID sapUuid;

	@ElementName("RunID")
	private String runId;

	@ElementName("ScheduleID")
	private String scheduleId;

	@ElementName("ScheduleID_Text")
	private String scheduleIdText;

	@ElementName("TemplateID")
	private String templateId;

	@ElementName("TemplateID_Text")
	private String templateIdText;

	@ElementName("DefinitionID")
	private String definitionId;

	@ElementName("DefinitionID_Text")
	private String definitionIdText;

	@ElementName("DefinitionTypeID")
	private String definitionTypeId;

	@ElementName("DefinitionTypeID_Text")
	private String definitionTypeIdText;

	@ElementName("ExecutionProgram")
	private String executionProgram;

	@ElementName("Status")
	private String status;

	@ElementName("Status_Text")
	private String statusText;

	@ElementName("FileName")
	private String fileName;

	@ElementName("Cancel_ac")
	private String cancelAction;

	@ElementName("Setnextstatus_ac")
	private String setNextStatusAction;

	@ElementName("ScheduledBy")
	private String scheduledBy;

	@ElementName("ScheduledOn")
	private String scheduledOn;

	@ElementName("ScheduledStart")
	private String scheduledStart;

	@ElementName("StartTime")
	private String startTime;

	@ElementName("EndTime")
	private String endTime;

	@Setter
	@ElementName("JobLogMessage")
	private String jobLogMessage;

	@ElementName("NoUser")
	private String noUser;

	@ElementName("Generated")
	private String generated;

	@ElementName("IsConsistent")
	private String isConsistent;

//	public UUID getSapUuid() {
//		return sapUuid;
//	}
//
//	public String getRunID() {
//		return runId;
//	}
//
//	public String getScheduleID() {
//		return scheduleId;
//	}
//
//	public String getScheduleIDText() {
//		return scheduleIdText;
//	}
//
//	public String getTemplateID() {
//		return templateId;
//	}
//
//	public String getTemplateIDText() {
//		return templateIdText;
//	}
//
//	public String getDefinitionID() {
//		return definitionId;
//	}
//
//	public String getDefinitionIDText() {
//		return definitionIdText;
//	}
//
//	public String getDefinitionTypeID() {
//		return definitionTypeId;
//	}
//
//	public String getDefinitionTypeIDText() {
//		return definitionTypeIdText;
//	}
//
//	public String getExecutionProgram() {
//		return executionProgram;
//	}
//
//	public String getStatus() {
//		return status;
//	}
//
//	public String getStatus_Text() {
//		return statusText;
//	}
//
//	public String getFileName() {
//		return fileName;
//	}
//
//	public String getCancelAction() {
//		return cancelAction;
//	}
//
//	public String getSetNextStatusAction() {
//		return setNextStatusAction;
//	}
//
//	public String getScheduledBy() {
//		return scheduledBy;
//	}
//
//	public String getScheduledOn() {
//		return scheduledOn;
//	}
//
	public LocalDateTime getScheduledStartAsDate() throws ParseException {
		if (scheduledStart == null) return null;
		String JSONDateToMilliseconds = "\\/Date\\((\\d+)([-+]\\d+)?\\)\\/";
		Pattern pattern = Pattern.compile(JSONDateToMilliseconds);
		Matcher matcher = pattern.matcher(scheduledStart);
      
		if (!matcher.find())
	        throw new ParseException("Wrong date time format " + scheduledStart, 0);
		String ts = matcher.replaceAll("$1");
		String tz = matcher.replaceAll("$2");
        final long millis = Long.parseLong(ts);
    	if (tz.isEmpty())
        	tz = "+0000";
      
      
		return new DateTime(millis, DateTimeZone.forID(tz)).toLocalDateTime();
	}
//
//	public String getStartTime() {
//		return startTime;
//	}
//
//	public String getEndTime() {
//		return endTime;
//	}
//
//	public String getJobLogMessage() {
//		return jobLogMessage;
//	}
//
//	public String getNoUser() {
//		return noUser;
//	}
//
//	public String getGenerated() {
//		return generated;
//	}
//
//	public String getIsConsistent() {
//		return isConsistent;
//	}

}