/**
  * Copyright 2017 aTool.org 
  */
package com.acuo.valuation.jackson;
import java.util.ArrayList;
import java.util.List;

import com.acuo.persist.entity.MarginCall;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class MarginCallDetail {

    @JsonProperty("uploadMarginCallDetails")
    private List<Uploadmargincalldetails> uploadmargincalldetails;



     public static MarginCallDetail of(Iterable<MarginCall> marginCalls)
     {
         MarginCallDetail marginCallDetail = new MarginCallDetail();
         marginCallDetail.uploadmargincalldetails = new ArrayList<Uploadmargincalldetails>();
         marginCalls.forEach(mc-> marginCallDetail.uploadmargincalldetails.add(Uploadmargincalldetails.of(mc)));
         return marginCallDetail;
     }

}