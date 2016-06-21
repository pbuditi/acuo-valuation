package com.acuo.valuation.markit.reports;

import javax.inject.Inject;

import com.acuo.common.marshal.Marshaller;

public class ReportParser {

	private final Marshaller marshaller;

	@Inject
	public ReportParser(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Report parse(String xml) {
		return null;// response;
	}

}
