package com.acuo.valuation.providers.datascope.protocol.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Credentials {

    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;
}
