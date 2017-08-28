package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.Trade;
import com.acuo.valuation.protocol.reports.Report;

import java.time.LocalDate;
import java.util.List;

public interface Sender {
    Report send(List<Trade> trades, LocalDate valuationDate);
}
