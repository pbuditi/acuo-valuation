package com.acuo.valuation.markit.reports;

import java.util.List;

public interface Report {

	public Header header();

	public List<Value> values();
}
