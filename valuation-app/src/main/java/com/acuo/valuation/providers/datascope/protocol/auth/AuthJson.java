package com.acuo.valuation.providers.datascope.protocol.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthJson {

    @JsonProperty("Credentials")
    private Credentials credentials;
}
