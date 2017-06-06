package com.acuo.valuation.jackson;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@lombok.Data
public class MarginCallResponse {

    @JsonProperty("uploadMarginCallDetails")
    private List<MarginCallResult> uploadmargincalldetails;


    public static MarginCallResponse of(Iterable<MarginCall> results) {
        final List<MarginCallResult> details = StreamSupport.stream(results.spliterator(), false)
                .map(MarginCallResult::of)
                .collect(toList());
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }

    public static MarginCallResponse ofPortfolio(Iterable<Portfolio> portfolios, TradeService tradeService, ValuationService valuationService)
    {
        final List<MarginCallResult> details = StreamSupport.stream(portfolios.spliterator(), false)
                .map(portfolio -> MarginCallResult.of(portfolio, tradeService, valuationService))
                .collect(toList());
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }
}