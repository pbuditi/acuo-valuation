package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.providers.clarus.protocol.RequestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class ClarusMarginServiceImpl implements ClarusMarginService {

    private final ClientEndPoint<ClarusEndPointConfig> clientEndPoint;
    private final ObjectMapper objectMapper;
    private final Transformer<SwapTrade> transformer;

    @Inject
    ClarusMarginServiceImpl(ClientEndPoint<ClarusEndPointConfig> clientEndPoint,
                            ObjectMapper objectMapper,
                            @Named("clarus") Transformer<SwapTrade> dataMapper) {
        this.clientEndPoint = clientEndPoint;
        this.objectMapper = objectMapper;
        this.transformer = dataMapper;
    }

    @Override
    public MarginResults send(List<SwapTrade> swaps, DataModel model, MarginCallType callType) {
        try {
            String request = makeRequest(swaps, model);
            String response = sendRequest(request, callType);
            final Set<String> portfolioIds = swaps.stream()
                    .map(swapTrade -> swapTrade.getInfo().getPortfolio())
                    .collect(toSet());
            return makeResult(response, portfolioIds, callType);
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
        log.info(request);
        return request;
    }

    private String sendRequest(String request, MarginCallType callType) {
        String response = ClarusCall.of(clientEndPoint, callType)
                .with("data", request)
                .create()
                .send();
        log.info(response);
        return response;
    }

    private MarginResults makeResult(String response, Set<String> portfolioIds, MarginCallType callType) throws IOException {
        List<?> list = transformer.deserialiseToList(response);
        List<Result<MarginValuation>> results = list.stream()
                .map(map -> (com.acuo.common.model.results.MarginValuation)map)
                .filter(map -> map.getPortfolioId() != null && portfolioIds.contains(map.getPortfolioId()))
                .map(map -> new MarginValuation(map.getName(),
                        map.getAccount(),
                        map.getChange(),
                        map.getTotal(),
                        callType.getCallType(),
                        map.getPortfolioId()))
                .map(Result::success)
                .collect(toList());

        MarginResults marginResults = new MarginResults();
        marginResults.setMarginType(callType.getCallType());
        marginResults.setResults(results);
        marginResults.setValuationDate(LocalDateUtils.minus(LocalDate.now(), 1));
        marginResults.setCurrency("USD");
        return marginResults;
    }
}