package com.acuo.valuation.reports.markit;

import java.util.ArrayList;
import java.util.List;

import com.acuo.valuation.reports.Header;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.reports.Value;

public class MarkitReport implements Report {

	private final Header header;
	private final List<Value> values;

	private MarkitReport(MarkitReportBuilder builder) {
		this.header = builder.header;
		this.values = builder.values;
	}

	@Override
	public Header header() {
		return header;
	}

	@Override
	public List<Value> values() {
		return values;
	}

	public static class MarkitReportBuilder {

		private final Header header;
		private final List<Value> values = new ArrayList<>();

		public MarkitReportBuilder(MarkitHeader header) {
			this.header = header;
		}

		public MarkitReportBuilder addValue(MarkitValue value) {
			values.add(value);
			return this;
		}

		public MarkitReportBuilder addValues(List<MarkitValue> value) {
			values.addAll(value);
			return this;
		}

		public MarkitReport build() {
			return new MarkitReport(this);
		}
	}

	@Override
	public String toString() {
		return "MarkitReport [header=" + header + ", values=" + values + "]";
	}
}
