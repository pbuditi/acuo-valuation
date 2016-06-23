package com.acuo.valuation.modules;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.Result;
import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PricingService.class).toInstance(new PricingService() {

			@Override
			public Result price(IrSwap swap) {
				return new Result() {
				};
			}
		});
	}

}
