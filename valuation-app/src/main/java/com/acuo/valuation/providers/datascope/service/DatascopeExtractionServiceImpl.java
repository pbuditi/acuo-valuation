package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.report.ReportResponseJson;
import com.acuo.valuation.providers.datascope.protocol.status.StatusResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> getExtractionFileId(String token, String scheduleId)
    {
        boolean statusReady = false;
        String reportExtractionId = null;
        do {
            String response = DatascopExtractionStatusCall.of(client).with("token", token).with("id", scheduleId).create().send();

            try
            {
                StatusResponseJson responseJson = objectMapper.readValue(response, StatusResponseJson.class);
                if(responseJson.getStatus().equalsIgnoreCase("Completed")) {
                    statusReady = true;
                    reportExtractionId = responseJson.getReportExtractionId();
                }
            }
            catch (Exception ioe)
            {
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException ie)
                {

                }
            }
        }while(!statusReady);

        String response = DatascopeExtractionReportCall.of(client).with("token", token).with("id", reportExtractionId).create().send();
        log.info(response);
        List<String> fileIds = new ArrayList<>();
        try
        {
            ReportResponseJson reportResponseJson = objectMapper.readValue(response, ReportResponseJson.class);
            reportResponseJson.getValue().stream().forEach(value -> fileIds.add(value.getExtractedfileid()));
        }
        catch(IOException ioe)
        {
            log.error("error in report:" + ioe);
        }
        return fileIds;
    }
}
