package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.protocol.results.MarkitResults;

import java.time.LocalDate;
import java.util.List;

public interface Retriever {
    MarkitResults retrieve(LocalDate localDate, List<String> tradeIds);
}
