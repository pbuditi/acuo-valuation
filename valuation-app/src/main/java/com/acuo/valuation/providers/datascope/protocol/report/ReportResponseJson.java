package com.acuo.valuation.providers.datascope.protocol.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReportResponseJson {
    @JsonProperty("value")
    private List<Value> value;
}
