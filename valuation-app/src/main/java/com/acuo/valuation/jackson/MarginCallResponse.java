package com.acuo.valuation.jackson;

import com.acuo.persist.entity.MarginCall;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Comparator;
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
                .sorted(Comparator.comparing(MarginCallResult::getMarginAgreement)
                        .thenComparing(marginCallResult -> marginCallResult.getAgreementDetails().getPricingSource()))
                .collect(toList());
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }

    public static MarginCallResponse ofPortfolio(List<MarginCallResult> details) {
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }
}