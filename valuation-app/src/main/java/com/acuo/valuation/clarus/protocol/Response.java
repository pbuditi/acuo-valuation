package com.acuo.valuation.clarus.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Response {
    @JsonProperty
    List<String> messages;

    @JsonProperty("stats")
    Map<String, String> stats;

    @JsonProperty("params")
    Map<String, String> params;

    @JsonProperty("results")
    Map<String, Map<String, Double>> results;
}
