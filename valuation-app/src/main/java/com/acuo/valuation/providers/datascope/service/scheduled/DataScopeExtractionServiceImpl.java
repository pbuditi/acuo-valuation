package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.report.ReportResponseJson;
import com.acuo.valuation.providers.datascope.protocol.report.Value;
import com.acuo.valuation.providers.datascope.protocol.status.StatusResponseJson;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.common.StringUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DataScopeExtractionServiceImpl implements DataScopeExtractionService {

    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    @Inject
    public DataScopeExtractionServiceImpl(ClientEndPoint<DataScopeEndPointConfig> client) {
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    public List<String> getExtractionFileId(String token, String scheduleId) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("scheduleId {} ", scheduleId);
            }
            String response = DataScopeExtractionStatusCall.of(client)
                    .with("token", token)
                    .with("id", scheduleId)
                    .retryWhile(this::isNotCompleted)
                    .create()
                    .send();

            StatusResponseJson responseJson = objectMapper.readValue(response, StatusResponseJson.class);
            String reportExtractionId = responseJson.getReportExtractionId();

            response = DataScopeExtractionReportCall.of(client)
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
        if (log.isDebugEnabled()) {
            log.debug("is not complete predicate [{}]", response);
        }
        return StringUtils.isEmpty(response) || !response.contains("Completed");
    }
}
