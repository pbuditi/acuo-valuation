package com.acuo.valuation.providers.datascope.protocol.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Trigger {

    @JsonProperty("@odata.type")
    private String odataType;
    @JsonProperty("LimitReportToTodaysData")
    private boolean limitreporttotodaysdata;
}
