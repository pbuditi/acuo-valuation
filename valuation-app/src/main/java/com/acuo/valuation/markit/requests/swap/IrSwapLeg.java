package com.acuo.valuation.markit.requests.swap;

class IrSwapLeg {

	private final int id;
	private final String currency;
	private final IrSwapLegFixing fixing;
	private final Double spread;
	private final Double rate;
	private final String type;
	private final String daycount;
	private final Double notional;
	private final String notionalxg;
	private final IrSwapLegPayDates dates;

	public IrSwapLeg(IrSwapLegInput input) {
		id = input.id;
		currency = input.currency;
		fixing = input.fixing != null ? new IrSwapLegFixing(input.fixing) : null;
		spread = input.spread;
		rate = input.rate;
		type = input.type;
		daycount = input.daycount;
		notional = input.notional;
		notionalxg = input.notionalxg;
		dates = new IrSwapLegPayDates(input.payDates);
	}

	public int id() {
		return id;
	}

	public String currency() {
		return currency;
	}

	public IrSwapLegFixing fixing() {
		return fixing;
	}

	public Double spread() {
		return spread;
	}

	public Double rate() {
		return rate;
	}

	public String type() {
		return type;
	}

	public String daycount() {
		return daycount;
	}

	public Double notional() {
		return notional;
	}

	public String notionalxg() {
		return notionalxg;
	}

	public IrSwapLegPayDates payDates() {
		return dates;
	}
}