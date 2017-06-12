package com.acuo.valuation.protocol.results;

import lombok.Data;

@Data
public class MarkitError {

    private final String tradeId;
    private final String error;

    public MarkitError(String tradeId, String error) {
        this.tradeId = tradeId;
        this.error = error;
    }

}
