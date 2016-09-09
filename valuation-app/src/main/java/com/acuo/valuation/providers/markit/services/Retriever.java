package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.protocol.results.PricingResults;

import java.time.LocalDate;
import java.util.List;

public interface Retriever {
    PricingResults retrieve(LocalDate localDate, List<String> tradeIds);
}
