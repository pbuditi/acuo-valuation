package com.acuo.valuation.services;

import com.acuo.valuation.providers.markit.product.swap.IrSwap;
import com.acuo.valuation.protocol.results.Result;

public interface PricingService {

    <T extends Result> T price(IrSwap swap);

}
