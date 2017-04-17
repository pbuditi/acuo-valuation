package com.acuo.valuation.providers.datascope.protocol.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Recurrence {

    @JsonProperty("@odata.type")
    private String odataType;
    @JsonProperty("IsImmediate")
    private boolean isimmediate;
}
