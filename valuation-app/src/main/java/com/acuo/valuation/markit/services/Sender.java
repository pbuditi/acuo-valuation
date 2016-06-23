package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.requests.dto.SwapDTO;

public interface Sender {
    Report send(IrSwap swap);
}
