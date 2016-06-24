package com.acuo.valuation.services;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.results.Result;

public interface PricingService {

	<T extends Result> T price(IrSwap swap);

}
