package com.acuo.valuation.markit.reports;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;

public class ReportDao {

	private static final Logger log = LoggerFactory.getLogger(ReportDao.class);

	private final Marshaller marshaller;

	@Inject
	public ReportDao(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Report parse(String xmlData) throws Exception {
		ArgChecker.notNull(xmlData, "xmlData");
		ReportDefinition def = marshaller.unmarshal(xmlData, ReportDefinition.class);
		return def.getReport();
	}

	public String parse(Report report) throws Exception {
		ArgChecker.notNull(report, "report");
		ReportDefinition definition = ReportDefinition.definition(report);
		return marshaller.marshal(definition);
	}
}