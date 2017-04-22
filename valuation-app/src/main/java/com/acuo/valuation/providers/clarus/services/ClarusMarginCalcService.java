package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.providers.clarus.protocol.RequestBuilder;
import com.acuo.valuation.providers.clarus.protocol.Response;
import com.acuo.valuation.services.MarginCalcService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;

@Slf4j
public class ClarusMarginCalcService implements MarginCalcService {

    private final ClientEndPoint clientEndPoint;
    private final ObjectMapper objectMapper;
    private final Transformer<SwapTrade> transformer;

    @Inject
    public ClarusMarginCalcService(ClientEndPoint<ClarusEndPointConfig> clientEndPoint, ObjectMapper objectMapper, @Named("clarus") Transformer<SwapTrade> dataMapper) {
        this.clientEndPoint = clientEndPoint;
        this.objectMapper = objectMapper;
        this.transformer = dataMapper;
    }

    @Override
    public MarginResults send(List<SwapTrade> swaps, DataModel model, MarginCallType callType) {
        try {
            String request = makeRequest(swaps, model);
            String response = sendRequest(request, callType);
            return makeResult(response);
        } catch (IOException e) {
            //TODO return an ErrorResult here instead of throwing an exception
            throw new RuntimeException("an error occurred while calculating margin: " + e.getMessage(), e);
        }
    }

    String makeRequest(List<SwapTrade> swaps, DataModel model) {
        TransformerContext context = new TransformerContext();
        context.setValueDate(LocalDate.now());
        String portfolios = transformer.serialise(swaps, context);
        String request = RequestBuilder
                .create(objectMapper, portfolios)
                .addDataModel(model)
                .build();
        log.debug(request);
        return request;
    }

    private String sendRequest(String request, MarginCallType callType) {
        String response = ClarusCall.of(clientEndPoint, callType)
                .with("data", request)
                .create()
                .send();
        log.debug(response);
        return response;
    }

    private MarginResults makeResult(String response) throws IOException {
        Response res = objectMapper.readValue(response, Response.class);
        List<Result<MarginValuation>> results = res.getResults().entrySet().stream().map(map -> new MarginValuation(map.getKey(),
                map.getValue().get("Account"),
                map.getValue().get("Change"),
                map.getValue().get("Margin"), null))
                .map(r -> Result.success(r))
                .collect(Collectors.toList());
        MarginResults marginResults = new MarginResults();
        marginResults.setResults(results);
        return marginResults;
    }


}