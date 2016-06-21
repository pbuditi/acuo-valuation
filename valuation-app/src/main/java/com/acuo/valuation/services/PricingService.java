package com.acuo.valuation.services;

import com.acuo.valuation.requests.dto.SwapDTO;

public interface PricingService {

	Result price(SwapDTO swap);

}
