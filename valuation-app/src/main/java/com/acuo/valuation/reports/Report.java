package com.acuo.valuation.reports;

import java.util.List;

public interface Report {

	public Header header();

	public List<Value> values();
}
