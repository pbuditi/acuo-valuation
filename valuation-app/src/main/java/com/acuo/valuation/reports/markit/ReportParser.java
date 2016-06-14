package com.acuo.valuation.reports.markit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.reports.Report;

public class ReportParser {

	private static final Logger log = LoggerFactory.getLogger(ReportParser.class);

	private final Marshaller marshaller;

	@Inject
	public ReportParser(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Report parse(String xmlData) throws Exception {
		ArgChecker.notNull(xmlData, "xmlData");
		ReportDefinition def = marshaller.unmarshal(xmlData, ReportDefinition.class);
		Report report = def.getReport();
		return report;
	}

	public String parse(Report report) throws Exception {
		ArgChecker.notNull(report, "report");
		ReportDefinition definition = ReportDefinition.definition(report);
		return marshaller.marshal(definition);
	}
}