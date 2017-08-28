package com.acuo.valuation.providers.datascope.service.intraday;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.persist.entity.FXRate;
import com.acuo.persist.services.FXRateService;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DataScopeIntradayServiceImpl implements DataScopeIntradayService {

    private final FXRateService fxRateService;
    private final DataScopeAuthService dataScopeAuthService;
    private final ClientEndPoint<DataScopeEndPointConfig> clientEndPoint;
    private final ObjectMapper objectMapper;

    @Inject
    DataScopeIntradayServiceImpl(FXRateService fxRateService,
                                 DataScopeAuthService dataScopeAuthService,
                                 ClientEndPoint<DataScopeEndPointConfig> clientEndPoint,
                                 ObjectMapper objectMapper) {
        this.fxRateService = fxRateService;
        this.dataScopeAuthService = dataScopeAuthService;
        this.clientEndPoint = clientEndPoint;
        this.objectMapper = objectMapper;
    }

    @Override
    public String rates() {
        String token = dataScopeAuthService.getToken();

        String response = IntradayCall.of(clientEndPoint)
                .with("token", token)
                .with("body", request())
                .create()
                .send();
        ExtractionResponse extraction = response(response);
        List<ExtractionResponse.Content> contents = extraction != null ? extraction.getContents() : null;
        if (contents != null) {
            log.info("saving {} rates", contents.size());
            contents.stream()
                    .map(content -> new FxRateParser().parser(content))
                    .filter(Objects::nonNull)
                    .forEach(parser -> saveFxRate(parser.getRate(), parser.getLastUpdate()));
        }
        return response;
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

    private void saveFxRate(FxRate fxRate, LocalDateTime lastUpdate) {
        final Currency base = fxRate.getPair().getBase();
        final Currency counter = fxRate.getPair().getCounter();
        FXRate fx = fxRateService.getOrCreate(base, counter);
        // workaround reuters wrong rate for JPYUSD=R and for USDKRW=R
        double value;
        if (CurrencyPair.of(Currency.USD, Currency.JPY).equals(fxRate.getPair()))
            value = fxRate.fxRate(fxRate.getPair()) / 100;
        else if (CurrencyPair.of(Currency.USD, Currency.KRW).equals(fxRate.getPair()))
            value = fxRate.fxRate(fxRate.getPair()) / 1000;
        else
            value = fxRate.fxRate(fxRate.getPair());
        fxRateService.addValue(fx, value, lastUpdate);
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
                return null;
            }
        }
    }
}