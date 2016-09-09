package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.reports.Report;

import java.util.List;

public interface Sender {
    Report send(List<SwapTrade> swap);
}
