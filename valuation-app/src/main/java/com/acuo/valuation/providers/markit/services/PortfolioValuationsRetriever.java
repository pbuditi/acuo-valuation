package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.protocol.responses.Response;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.Value;
import com.opengamma.strata.collect.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioValuationsRetriever implements Retriever {

    private static final String ERROR_MSG = "Error occurred while retrieving markit results for the date {}";

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsRetriever.class);

    private final ClientEndPoint client;
    private final ResponseParser parser;

    @Inject
    public PortfolioValuationsRetriever(ClientEndPoint<MarkitEndPointConfig> client, ResponseParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public PricingResults retrieve(LocalDate valuationDate, List<String> tradeIds) {
        //return PricingResults.of(tradeIds.stream().map(id -> retrieve(valuationDate, id)).collect(Collectors.toList()));
        Response results = retrieve(valuationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        List<Value> values = new ArrayList<Value>();
        for(Value value : results.values())
        {
            for(String tradeId : tradeIds)
            {
                if(value.getTradeId().equals(tradeId))
                {
                    values.add(value);
                    break;
                }
            }
        }
        Result<MarkitValuation> result = Result.success(new MarkitValuation(values.toArray(new Value[values.size()])));
        List<Result<MarkitValuation>> resultList = new ArrayList<>();
        resultList.add(result);

        PricingResults pricingResults = PricingResults.of(resultList);
        pricingResults.setDate(LocalDateToDate(results.header().getDate()));
        pricingResults.setCurrency(results.header().getValuationCurrency());

        return  pricingResults;

    }

    private Result<MarkitValuation> retrieve(LocalDate valuationDate, String tradeId) {
        Response results = retrieve(valuationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        List<Value> resultList = results.values().stream().filter(v -> tradeId.equals(v.getTradeId())).collect(Collectors.toList());
        return Result.success(new MarkitValuation(resultList.toArray(new Value[resultList.size()])));
    }



    /**
     * Retrieve the results for a given valuation date
     *
     * @param asOfDate, date in DDMONYY or YYYY-MM-DD format
     * @return response, parser from the markit report
     */
    Response retrieve(String asOfDate) {
        try {
            String result = MarkitFormCall.of(client)
                                          .with("asof", asOfDate)
                                          .with("format", "xml")
                                          .retryWhile(s -> s.contains("<valuationscomplete>false</valuationscomplete>"))
                                          .create()
                                          .send();
            if (LOG.isDebugEnabled()) LOG.debug(result);
            return parser.parse(result);
        } catch (Exception e) {
            LOG.error(ERROR_MSG, asOfDate, e);
            throw new RuntimeException(String.format(ERROR_MSG, asOfDate), e);
        }
    }

    public static Date LocalDateToDate(LocalDate localDate)
    {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }

}