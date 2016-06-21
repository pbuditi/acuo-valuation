package com.acuo.valuation.markit.services;

import com.acuo.valuation.responses.Value;
import com.acuo.valuation.services.Result;

import lombok.Data;

@Data
public class SwapResult implements Result {

	private final Double pv;

	public SwapResult(Value value) {
		this.pv = value.getPv();
	}

}
