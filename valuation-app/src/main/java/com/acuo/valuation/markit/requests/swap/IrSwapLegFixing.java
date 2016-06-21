package com.acuo.valuation.markit.requests.swap;

class IrSwapLegFixing {

	private final String name;
	private final String term;
	private final boolean arrears;

	public IrSwapLegFixing(IrSwapLegFixingInput input) {
		name = input.name;
		term = input.term;
		arrears = input.arrears;
	}

	public String name() {
		return name;
	}

	public String term() {
		return term;
	}

	public boolean isArrears() {
		return arrears;
	}
}