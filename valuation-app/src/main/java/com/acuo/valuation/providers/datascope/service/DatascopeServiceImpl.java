package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.auth.AuthJson;
import com.acuo.valuation.providers.datascope.protocol.auth.Credentials;
import com.acuo.valuation.providers.datascope.protocol.auth.TokenJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class DatascopeServiceImpl implements DatascopeService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    @Inject
    public DatascopeServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    public String getToken()
    {
        String token = null;
        String response = DatascopeAuthCall.of(client).with("josn", getAuthJsonString()).create().send();
        try
        {
            token = objectMapper.readValue(response, TokenJson.class).getValue();
        }
        catch (IOException ioe)
        {
            log.error("error in getToekn:" + ioe);
        }
        log.info("token is :" + token);
        return token;
    }

    private String getAuthJsonString()
    {
        DatascopeEndPointConfig config = client.config();
        Credentials credentials = new Credentials();
        credentials.setUsername(config.getUsername());
        credentials.setPassword(config.getPassword());
        AuthJson authJson = new AuthJson();
        authJson.setCredentials(credentials);

        String value= "";
        try
        {
            value = objectMapper.writeValueAsString(authJson);
        }
        catch (JsonProcessingException jpe)
        {
            log.error("error in auth json :" + jpe);
        }
        return value;
    }
}
