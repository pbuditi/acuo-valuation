package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.status.StatusResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class DatascopeExtractionServiceImpl implements DatascopeExtractionService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    @Inject
    public DatascopeExtractionServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        objectMapper = new ObjectMapper();
    }
    public String getExtractionFileId(String token, String scheduleId)
    {
        boolean statusReady = false;
        do {
            String response = DatascopExtractionStatusCall.of(client).with("token", token).with("id", scheduleId).create().send();
            log.info(response);
            try
            {
                String status = objectMapper.readValue(response, StatusResponseJson.class).getStatus();
                if(status.equalsIgnoreCase("Completed"))
                    statusReady = true;
                Thread.sleep(5000);
            }
            catch (Exception ioe)
            {
                log.error("error in getExtractionStatus:" + ioe);
            }
        }while(!statusReady);
        return null;
    }
}
