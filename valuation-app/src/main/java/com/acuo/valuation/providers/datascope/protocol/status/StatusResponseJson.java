package com.acuo.valuation.providers.datascope.protocol.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class StatusResponseJson {

    @JsonProperty("ReportExtractionId")
    private String reportExtractionId;
    @JsonProperty("ScheduleId")
    private String scheduleid;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("DetailedStatus")
    private String detailedstatus;
    @JsonProperty("ScheduleName")
    private String schedulename;
    @JsonProperty("IsTriggered")
    private boolean istriggered;
}
