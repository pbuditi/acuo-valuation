package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.responses.ResponseParser;
import com.acuo.valuation.responses.Response;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.results.SwapResult;
import com.acuo.valuation.results.Value;
import com.acuo.valuation.services.ClientEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioValuationsRetriever implements Retriever {

    private static final String ERROR_MSG = "Error occurred while retrieving markit results for the date {}";

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsRetriever.class);

    private final ClientEndPoint client;
    private final ResponseParser parser;

    @Inject
    public PortfolioValuationsRetriever(ClientEndPoint client, ResponseParser parser) {
        this.client = client;
        this.parser = parser;
    }

    public Result retrieve(LocalDate valuationDate, String tradeId) {
        Response results = retrieve(valuationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        List<Value> resultList = results.values().stream().filter(v -> tradeId.equals(v.getTradeId())).collect(Collectors.toList());
        return new SwapResult(resultList.toArray(new Value[resultList.size()]));
    }

    /**
     * Retrieve the results for a given valuation date
     *
     * @param asOfDate, date in DDMONYY or YYYY-MM-DD format
     * @return response, parser from the markit report
     */
    Response retrieve(String asOfDate) {
        try {
            String result = client.get().with("asof", asOfDate).with("format", "xml").send();
            if (LOG.isDebugEnabled()) LOG.debug(result);
            return parser.parse(result);
        } catch (Exception e) {
            LOG.error(ERROR_MSG, asOfDate, e);
            throw new RuntimeException(String.format(ERROR_MSG, asOfDate), e);
        }
    }
}