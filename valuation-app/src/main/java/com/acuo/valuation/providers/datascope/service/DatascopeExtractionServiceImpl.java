package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.report.ReportResponseJson;
import com.acuo.valuation.providers.datascope.protocol.report.Value;
import com.acuo.valuation.providers.datascope.protocol.status.StatusResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DatascopeExtractionServiceImpl implements DatascopeExtractionService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    @Inject
    public DatascopeExtractionServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client) {
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    public List<String> getExtractionFileId(String token, String scheduleId) {
        try {
            String response = DatascopExtractionStatusCall.of(client)
                    .with("token", token)
                    .with("id", scheduleId)
                    .retryWhile(this::isNotCompleted)
                    .create()
                    .send();

            StatusResponseJson responseJson = objectMapper.readValue(response, StatusResponseJson.class);
            String reportExtractionId = responseJson.getReportExtractionId();

            response = DatascopeExtractionReportCall.of(client)
                    .with("token", token)
                    .with("id", reportExtractionId)
                    .create().send();

            log.info(response);
            ReportResponseJson reportResponseJson = objectMapper.readValue(response, ReportResponseJson.class);
            List<String> fileIds = reportResponseJson.getValue()
                    .stream()
                    .map(Value::getExtractedfileid)
                    .collect(toList());
            return fileIds;
        } catch (IOException ioe) {
            log.error("error in report:" + ioe);
            return Collections.emptyList();
        }
    }

    private boolean isNotCompleted(String response) {
        try {
            StatusResponseJson responseJson = objectMapper.readValue(response, StatusResponseJson.class);
            String status = responseJson.getStatus();
            return !status.equalsIgnoreCase("Completed");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
