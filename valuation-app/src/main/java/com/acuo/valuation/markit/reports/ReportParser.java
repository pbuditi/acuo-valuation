package com.acuo.valuation.markit.reports;

import javax.inject.Inject;
import javax.inject.Named;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.reports.Report;

public class ReportParser {

	private final Marshaller marshaller;

	@Inject
	public ReportParser(@Named("xml") Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Report parse(String xml) throws Exception {
		ArgChecker.notNull(xml, "xml");
		ReportInput definition = marshaller.unmarshal(xml, ReportInput.class);
		return definition.report();
	}

	public String parse(Report report) throws Exception {
		ArgChecker.notNull(report, "report");
		ReportInput definition = ReportInput.definition(report);
		return marshaller.marshal(definition);
	}
}
