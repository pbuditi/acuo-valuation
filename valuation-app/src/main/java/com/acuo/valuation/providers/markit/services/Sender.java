package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.protocol.reports.Report;

import java.time.LocalDate;
import java.util.List;

public interface Sender {
    Report send(List<com.acuo.common.model.trade.Trade> trades, LocalDate valuationDate);
}
