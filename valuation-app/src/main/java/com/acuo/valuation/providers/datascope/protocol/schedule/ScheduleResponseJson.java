package com.acuo.valuation.providers.datascope.protocol.schedule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ScheduleResponseJson {

    @JsonProperty("@odata.context")
    private String odataContext;
    @JsonProperty("ScheduleId")
    private String scheduleId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("TimeZone")
    private String timezone;
    @JsonProperty("Recurrence")
    private Recurrence recurrence;
    @JsonProperty("Trigger")
    private Trigger trigger;
    @JsonProperty("UserId")
    private int userid;
    @JsonProperty("ListId")
    private String listId;
    @JsonProperty("ReportTemplateId")
    private String reportTemplateid;
}
