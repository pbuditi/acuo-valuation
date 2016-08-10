package com.acuo.valuation.protocol.responses;

import java.time.LocalDate;

public interface Header {

    String getName();

    String getVersion();

    LocalDate getDate();

    LocalDate getValuationDate();

    String getValuationCurrency();

    Integer getSuccessfulTrades();

    Integer getFailedTrades();

    Integer getTotalTrades();

    Boolean getValuationsComplete();
}