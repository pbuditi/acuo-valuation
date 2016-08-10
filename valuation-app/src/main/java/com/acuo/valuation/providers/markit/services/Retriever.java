package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.protocol.results.Result;

import java.time.LocalDate;

public interface Retriever {
    Result retrieve(LocalDate localDate, String tradeId);
}
