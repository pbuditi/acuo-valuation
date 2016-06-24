package com.acuo.valuation.results;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class SwapResult implements Result {

	private final List<Value> values;

	public SwapResult(Value... values) {
		this.values = Arrays.asList(values);
	}

	public Double getPv() {
		return values.stream().map(Value::getPv).reduce(0.0d, (a, b) -> a + b);
	}

}
