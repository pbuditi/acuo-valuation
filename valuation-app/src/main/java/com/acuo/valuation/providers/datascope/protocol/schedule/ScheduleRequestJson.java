package com.acuo.valuation.providers.datascope.protocol.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ScheduleRequestJson {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Recurrence")
    private Recurrence recurrence;
    @JsonProperty("Trigger")
    private Trigger trigger;
    @JsonProperty("ListId")
    private String listId;
    @JsonProperty("ReportTemplateId")
    private String reportTemplateId;
}
