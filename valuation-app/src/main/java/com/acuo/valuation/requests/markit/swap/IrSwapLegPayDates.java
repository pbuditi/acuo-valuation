package com.acuo.valuation.requests.markit.swap;

import java.util.Date;

class IrSwapLegPayDates {

	private final Date startDate;
	private final String frequency;
	private final Date enddate;
	private final String rollCode;
	private final boolean adjust;
	private final boolean eom;

	public IrSwapLegPayDates(IrSwapLegPayDatesInput input) {
		startDate = input.startDate;
		frequency = input.frequency;
		enddate = input.enddate;
		rollCode = input.rollCode;
		adjust = input.adjust;
		eom = input.eom;
	}

	public Date startDate() {
		return startDate;
	}

	public String frequency() {
		return frequency;
	}

	public Date enddate() {
		return enddate;
	}

	public String rollCode() {
		return rollCode;
	}

	public boolean isAdjust() {
		return adjust;
	}

	public boolean isEom() {
		return eom;
	}
}