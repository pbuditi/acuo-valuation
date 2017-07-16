package com.acuo.valuation.jackson;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Portfolio;
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

    public static MarginCallResponse ofPortfolio(List<MarginCallResult> details) {
        MarginCallResponse marginCallResponse = new MarginCallResponse();
        marginCallResponse.setUploadmargincalldetails(details);
        return marginCallResponse;
    }
}