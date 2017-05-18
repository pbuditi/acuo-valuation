package com.acuo.valuation.providers.datascope.service.authentication;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.auth.AuthJson;
import com.acuo.valuation.providers.datascope.protocol.auth.Credentials;
import com.acuo.valuation.providers.datascope.protocol.auth.TokenJson;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class DataScopeAuthServiceImpl implements DataScopeAuthService {

    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    private String token;

    @Inject
    public DataScopeAuthServiceImpl(ClientEndPoint<DataScopeEndPointConfig> client)
    {
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    public String getToken()
    {
        String response = DataScopeAuthCall.of(client).with("josn", buildAuthJson()).create().send();
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



    private String buildAuthJson()
    {
        DataScopeEndPointConfig config = client.config();
        Credentials credentials = new Credentials();
        credentials.setUsername(config.getUsername());
        credentials.setPassword(config.getPassword());
        AuthJson authJson = new AuthJson();
        authJson.setCredentials(credentials);

        String value= null;
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
