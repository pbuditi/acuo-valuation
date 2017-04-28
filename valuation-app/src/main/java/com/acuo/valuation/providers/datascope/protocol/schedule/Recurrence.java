package com.acuo.valuation.providers.datascope.protocol.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Recurrence {

    @JsonProperty("@odata.type")
    private String odataType;
    @JsonProperty("IsImmediate")
    private boolean isimmediate;

}
