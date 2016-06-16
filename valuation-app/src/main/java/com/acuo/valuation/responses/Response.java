package com.acuo.valuation.responses;

import java.util.List;

public interface Response {

	public Header header();

	public List<Value> values();
}
