package com.acuo.valuation.markit.requests.swap;

import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class IrSwap {

	private String tradeId;
	private LocalDate tradeDate;
	private String book;
	private Set<IrSwapLeg> legs;

	public IrSwap() { }

	public IrSwap(IrSwapInput input) {
		tradeId = input.tradeId;
		tradeDate = input.tradeDate;
		book = input.book;
		legs = input.legs.stream().map(leg -> new IrSwapLeg(leg)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public String tradeId() {
		return tradeId;
	}

	public LocalDate tradeDate() {
		return tradeDate;
	}

	public String book() {
		return book;
	}

	public Set<IrSwapLeg> legs() {
		return legs;
	}
}