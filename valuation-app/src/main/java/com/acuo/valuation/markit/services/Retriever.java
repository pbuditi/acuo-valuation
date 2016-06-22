package com.acuo.valuation.markit.services;

import com.acuo.valuation.services.Result;

import java.time.LocalDate;

public interface Retriever {
    Result retrieve(LocalDate localDate, String tradeId);
}
