package com.acuo.valuation.responses;

import com.acuo.valuation.results.Value;

import java.util.List;

public interface Response {

	public Header header();

	public List<Value> values();
}
