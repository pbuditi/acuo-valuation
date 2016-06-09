package com.acuo.valuation.markit.reports;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.valuation.markit.reports.MarkitReport.MarkitReportBuilder;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportDefinition {

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
	HeaderDefinition header;

	static class ValueDefinition {

		@XmlPath("TradeId/text()")
		String tradeId;

		@XmlPath("Book/text()")
		String book;

		@XmlPath("PortfolioValuationsLocal/text()")
		String pvLocal;

		@XmlPath("PresentValue/text()")
		String pv;

		@XmlPath("Accrued/text()")
		String accrued;
	}

	@XmlElement(name = "value")
	List<ValueDefinition> values;

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
		return value;
	}
}
