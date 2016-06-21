package com.acuo.valuation.markit.services;

import java.time.LocalDate;

import javax.inject.Inject;

import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.Result;

public class MarkitPricingService implements PricingService {

	private final PortfolioValuationsSender sender;
	private final PortfolioValuationsRetriever retriever;

	@Inject
	MarkitPricingService(PortfolioValuationsSender sender, PortfolioValuationsRetriever retriever) {
		this.sender = sender;
		this.retriever = retriever;
	}

	@Override
	public Result price(SwapDTO swap) {

		LocalDate valuationDate = sender.send(swap);

		return retriever.retrieve(valuationDate, swap.getTradeId());
	}

}
