package com.acuo.valuation.markit.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.common.marshal.jaxb.DateAdapter;
import com.acuo.common.marshal.jaxb.DecimalAdapter;
import com.acuo.valuation.markit.reports.MarkitReport.MarkitReportBuilder;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportDefinition {

	public ReportDefinition() {
		header = new HeaderDefinition();
		values = new ArrayList<>();
	}

	static class HeaderDefinition {

		@XmlPath("name/text()")
		String name;

		@XmlPath("version/text()")
		String version;

		@XmlPath("date/text()")
		@XmlJavaTypeAdapter(DateAdapter.class)
		Date date;

		@XmlPath("valuationdate/text()")
		@XmlJavaTypeAdapter(DateAdapter.class)
		Date valuationDate;

		@XmlPath("valuationccy/text()")
		String valuationCurrency;

		@XmlPath("numtradessuccessful/text()")
		Integer successfulTrades;

		@XmlPath("numtradesfailed/text()")
		Integer failedTrades;

		@XmlPath("totalnumtrades/text()")
		Integer totalTrades;

		@XmlPath("valuationscomplete/text()")
		Boolean valuationsComplete;
	}

	@XmlElement(name = "header", required = true)
	public final HeaderDefinition header;

	static class ValueDefinition {

		@XmlPath("TradeId/text()")
		String tradeId;

		@XmlPath("Book/text()")
		String book;

		@XmlPath("PortfolioValuationsLocal/text()")
		@XmlJavaTypeAdapter(DecimalAdapter.class)
		Double pvLocal;

		@XmlPath("PresentValue/text()")
		@XmlJavaTypeAdapter(DecimalAdapter.class)
		Double pv;

		@XmlPath("Accrued/text()")
		@XmlJavaTypeAdapter(DecimalAdapter.class)
		Double accrued;

		@XmlPath("ParRate/text()")
		@XmlJavaTypeAdapter(DecimalAdapter.class)
		Double parRate;

		@XmlPath("LocalCcy/text()")
		String localCurrency;

		@XmlPath("Status/text()")
		String status;

		@XmlPath("LegId/text()")
		String legId;

		@XmlPath("Notional/text()")
		@XmlJavaTypeAdapter(DecimalAdapter.class)
		Double notional;
	}

	@XmlElement(name = "value")
	public final List<ValueDefinition> values;

	public static ReportDefinition definition(Report report) {
		return new ReportDefinitionBuilder(report).build();
	}

	public static class ReportDefinitionBuilder {

		private final Report report;

		public ReportDefinitionBuilder(Report report) {
			this.report = report;
		}

		public ReportDefinition build() {
			ReportDefinition definition = new ReportDefinition();
			populateHeader(definition);
			populateValues(definition);
			return definition;
		}

		private void populateHeader(ReportDefinition definition) {
			HeaderDefinition header = definition.header;
			Header h = report.header();
			header.date = h.getDate();
			header.failedTrades = h.getFailedTrades();
			header.name = h.getName();
			header.successfulTrades = h.getSuccessfulTrades();
			header.totalTrades = h.getTotalTrades();
			header.valuationCurrency = h.getValuationCurrency();
			header.valuationDate = h.getValuationDate();
			header.valuationsComplete = h.getValuationsComplete();
			header.version = h.getVersion();
		}

		private void populateValues(ReportDefinition definition) {
			report.values().stream().forEach(v -> populateValue(v, definition));
		}

		private void populateValue(Value v, ReportDefinition definition) {
			ValueDefinition value = new ReportDefinition.ValueDefinition();
			value.accrued = v.getAccrued();
			value.book = v.getBook();
			value.legId = v.getLegId();
			value.localCurrency = v.getLocalCurrency();
			value.notional = v.getNotional();
			value.parRate = v.getParRate();
			value.pv = v.getPv();
			value.pvLocal = v.getPvLocal();
			value.status = v.getStatus();
			value.tradeId = v.getTradeId();

			definition.values.add(value);
		}
	}

	public Report getReport() {
		MarkitReportBuilder builder = new MarkitReportBuilder(header());
		addValues(builder);
		return builder.build();
	}

	private MarkitHeader header() {
		MarkitHeader markitHeader = new MarkitHeader();
		markitHeader.setName(header.name);
		markitHeader.setVersion(header.version);
		markitHeader.setDate(header.date);
		markitHeader.setValuationDate(header.valuationDate);
		markitHeader.setValuationCurrency(header.valuationCurrency);
		markitHeader.setSuccessfulTrades(header.successfulTrades);
		markitHeader.setFailedTrades(header.failedTrades);
		markitHeader.setTotalTrades(header.totalTrades);
		markitHeader.setValuationsComplete(header.valuationsComplete);
		return markitHeader;
	}

	private void addValues(MarkitReportBuilder builder) {
		values.stream().map(valueDef -> value(valueDef)).forEach(value -> builder.addValue(value));
	}

	private MarkitValue value(ValueDefinition v) {
		MarkitValue value = new MarkitValue();
		value.setTradeId(v.tradeId);
		value.setBook(v.book);
		value.setPvLocal(v.pvLocal);
		value.setPv(v.pv);
		value.setAccrued(v.accrued);
		value.setParRate(v.parRate);
		value.setLocalCurrency(v.localCurrency);
		value.setStatus(v.status);
		value.setLegId(v.legId);
		value.setNotional(v.notional);
		return value;
	}
}
