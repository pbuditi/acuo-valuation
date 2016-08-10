package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.providers.markit.product.swap.IrSwap;
import com.acuo.valuation.protocol.reports.Report;

public interface Sender {
    Report send(IrSwap swap);
}
