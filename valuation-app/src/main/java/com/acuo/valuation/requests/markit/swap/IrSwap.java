package com.acuo.valuation.requests.markit.swap;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IrSwap {

	private final String tradeId;
	private final Date tradeDate;
	private final String book;
	private final Set<IrSwapLeg> legs;

	public IrSwap(IrSwapInput input) {
		tradeId = input.tradeId;
		tradeDate = input.tradeDate;
		book = input.book;
		legs = input.legs.stream().map(leg -> new IrSwapLeg(leg)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public String tradeId() {
		return tradeId;
	}

	public Date tradeDate() {
		return tradeDate;
	}

	public String book() {
		return book;
	}

	public Set<IrSwapLeg> legs() {
		return legs;
	}
}