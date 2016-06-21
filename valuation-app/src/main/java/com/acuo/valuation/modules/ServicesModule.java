package com.acuo.valuation.modules;

import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.Result;
import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PricingService.class).toInstance(new PricingService() {

			@Override
			public Result price(SwapDTO swap) {
				return new Result() {
				};
			}
		});
	}

}
