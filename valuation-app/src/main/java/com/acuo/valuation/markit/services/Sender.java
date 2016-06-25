package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.reports.Report;

public interface Sender {
    Report send(IrSwap swap);
}
