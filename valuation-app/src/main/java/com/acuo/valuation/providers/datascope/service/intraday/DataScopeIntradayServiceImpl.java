package com.acuo.valuation.providers.datascope.service.intraday;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengamma.strata.basics.currency.FxRate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DataScopeIntradayServiceImpl implements DataScopeIntradayService {

    private final DataScopeAuthService dataScopeAuthService;
    private final ClientEndPoint<DataScopeEndPointConfig> clientEndPoint;
    private final ObjectMapper objectMapper;

    @Inject
    public DataScopeIntradayServiceImpl(DataScopeAuthService dataScopeAuthService,
                                        ClientEndPoint<DataScopeEndPointConfig> clientEndPoint,
                                        ObjectMapper objectMapper) {
        this.dataScopeAuthService = dataScopeAuthService;
        this.clientEndPoint = clientEndPoint;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<FxRate> rates() {
        String token = dataScopeAuthService.getToken();

        String response = IntradayCall.of(clientEndPoint)
                .with("token", token)
                .with("body", request())
                .create()
                .send();
        ExtractionResponse extraction = response(response);
        return extraction.getContents().stream()
                .map(content -> new FxRateParser().parser(content))
                .map(fxRateParser -> fxRateParser.getRate())
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private String request() {
        ExtractionRequest request = new ExtractionRequest();
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private ExtractionResponse response(String response) {
        try {
            return objectMapper.readValue(response, ExtractionResponse.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Data
    static class FxRateParser {

        private static final String PATTERN = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

        private FxRate rate;
        private LocalDateTime lastUpdate;

        FxRateParser() {}

        FxRateParser parser(ExtractionResponse.Content content) {
            try {
                String identifier = content.getIdentifier();
                String counter = identifier.substring(0, 3);
                String base = identifier.substring(3, 6);
                this.rate = FxRate.parse(base + "/" + counter + " " + content.getMidPrice());
                this.lastUpdate = content.getLastUpdatedTime();
                return this;
            } catch (Exception e) {
                log.error(e.getMessage());
                return this;
            }
        }
    }
}
