package com.acuo.valuation.jackson;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Data
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

    public static MarginCallResponse ofPortfolio(Iterable<Portfolio> portfolios, ValuationService valuationService) {
        final List<MarginCallResult> details = StreamSupport.stream(portfolios.spliterator(), false)
                .map(portfolio -> {
                    Agreement agreement = portfolio.getAgreement();
                    String type = agreement.getType();
                    if ("bilateral".equals(type) || "legacy".equals(type)) {
                        MarginCallResult callResult = new MarginCallResult.BilateralBuilder(portfolio, valuationService).build();
                        return ImmutableList.of(callResult);
                    } else {
                        MarginCallResult vm = new MarginCallResult.ClearedVMBuilder(portfolio, valuationService).build();
                        MarginCallResult im = new MarginCallResult.ClearedIMBuilder(portfolio, valuationService).build();
                        return ImmutableList.of(vm, im);
                    }
                })
                .flatMap(Collection::stream)
                .collect(toList());
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }
}