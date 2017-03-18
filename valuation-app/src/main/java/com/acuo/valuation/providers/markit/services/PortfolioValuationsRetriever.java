package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.protocol.responses.Response;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.Value;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class PortfolioValuationsRetriever implements Retriever {

    private static final String ERROR_MSG = "Error occurred while retrieving markit results for the date {}";
    private static final DateTimeFormatter VALUATION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClientEndPoint<MarkitEndPointConfig> client;
    private final ResponseParser parser;

    @Inject
    public PortfolioValuationsRetriever(ClientEndPoint<MarkitEndPointConfig> client, ResponseParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public PricingResults retrieve(LocalDate valuationDate, List<String> tradeIds) {
        final Response response = upload(valuationDate, tradeIds);

        printFailedTrades(tradeIds, response);

        List<Result<MarkitValuation>> results = tradeIds.stream()
                .map(tradeId -> response.values()
                        .stream()
                        .filter(value -> tradeId.equals(value.getTradeId()))
                        .filter(value -> !"Failed".equalsIgnoreCase(value.getStatus()))
                        .collect(toList()))
                .map(MarkitValuation::new)
                .map(Result::success)
                .collect(toList());

        PricingResults pricingResults = new PricingResults();
        pricingResults.setResults(results);
        pricingResults.setDate(response.header().getDate());
        pricingResults.setCurrency(Currency.parse(response.header().getValuationCurrency()));

        return pricingResults;
    }

    private void printFailedTrades(List<String> tradeIds, Response response) {
        List<String> values = response.values()
                .stream()
                .filter(value -> tradeIds.contains(value.getTradeId()))
                .filter(value -> "Failed".equalsIgnoreCase(value.getStatus()))
                .map(Value::getTradeId)
                .collect(toList());
        log.warn("valuation for the following trades has failed: {}",values);
    }

    private Response upload(LocalDate valuationDate, List<String> tradeIds) {
        String asOfDate = valuationDate.format(VALUATION_DATE_FORMAT);
        try {
            String result = MarkitFormCall.of(client)
                    .with("asof", asOfDate)
                    .with("format", "xml")
                    .retryWhile(s -> !isValuationDone(s, tradeIds))
                    .create()
                    .send();
            if (log.isTraceEnabled()) {
                log.trace(result);
            }
            return parser.parse(result);
        } catch (Exception e) {
            log.error(ERROR_MSG, asOfDate, e);
            throw new RuntimeException(String.format(ERROR_MSG, asOfDate), e);
        }
    }

    private boolean isValuationDone(String s, List<String> tradeIds) {
        for (String tradeId : tradeIds) {
            if (!s.contains("<TradeId>" + tradeId + "</TradeId>"))
                return false;
        }
        log.info("valuation done for {}", tradeIds);
        return true;
    }
}