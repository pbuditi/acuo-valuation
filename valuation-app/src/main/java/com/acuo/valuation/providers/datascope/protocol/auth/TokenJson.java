package com.acuo.valuation.providers.datascope.protocol.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenJson {

    @JsonProperty("@odata.context")
    private String odataContext;
    @JsonProperty("value")
    private String value;
}
