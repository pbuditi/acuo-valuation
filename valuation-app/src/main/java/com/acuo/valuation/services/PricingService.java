package com.acuo.valuation.services;

import com.acuo.valuation.markit.requests.swap.IrSwap;

public interface PricingService {

	Result price(IrSwap swap);

}
