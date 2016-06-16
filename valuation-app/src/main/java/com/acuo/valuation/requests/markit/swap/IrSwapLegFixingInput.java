package com.acuo.valuation.requests.markit.swap;

import org.eclipse.persistence.oxm.annotations.XmlPath;

public class IrSwapLegFixingInput {

	public IrSwapLegFixingInput() {

	}

	public IrSwapLegFixingInput(IrSwapLegFixing fixing) {
		name = fixing.name();
		term = fixing.term();
		arrears = fixing.isArrears();
	}

	@XmlPath("name/text()")
	public String name;

	@XmlPath("term/text()")
	public String term;

	@XmlPath("arrears/text()")
	public boolean arrears;

}