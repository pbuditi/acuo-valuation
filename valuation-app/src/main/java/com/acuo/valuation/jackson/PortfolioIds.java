package com.acuo.valuation.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PortfolioIds {

    @JsonProperty("ids")
    private List<String> ids;
}
