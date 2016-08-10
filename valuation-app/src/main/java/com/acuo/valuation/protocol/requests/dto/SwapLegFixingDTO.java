package com.acuo.valuation.protocol.requests.dto;

import lombok.Data;

@Data
public class SwapLegFixingDTO {

    private String name;
    private String term;
    private boolean arrears;

}
