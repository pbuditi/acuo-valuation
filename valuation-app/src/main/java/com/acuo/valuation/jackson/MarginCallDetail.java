package com.acuo.valuation.jackson;

import java.util.ArrayList;
import java.util.List;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.VariationMargin;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class MarginCallDetail {

    @JsonProperty("uploadMarginCallDetails")
    private List<Uploadmargincalldetails> uploadmargincalldetails;


    public static MarginCallDetail of(Iterable<VariationMargin> marginCalls) {
        MarginCallDetail marginCallDetail = new MarginCallDetail();
        marginCallDetail.uploadmargincalldetails = new ArrayList<>();
        marginCalls.forEach(mc -> marginCallDetail.uploadmargincalldetails.add(Uploadmargincalldetails.of(mc)));
        return marginCallDetail;
    }
}