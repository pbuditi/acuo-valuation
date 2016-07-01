package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.product.swap.IrSwap;
import com.acuo.valuation.reports.Report;

public interface Sender {
    Report send(IrSwap swap);
}
