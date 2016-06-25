package com.acuo.valuation.markit.responses;

import com.acuo.valuation.responses.Header;
import com.acuo.valuation.responses.Response;
import com.acuo.valuation.results.Value;

import java.util.ArrayList;
import java.util.List;

public class MarkitResponse implements Response {

	private final Header header;
	private final List<Value> values;

	private MarkitResponse(MarkitResponseBuilder builder) {
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

	public static class MarkitResponseBuilder {

		private final Header header;
		private final List<Value> values = new ArrayList<>();

		public MarkitResponseBuilder(MarkitHeader header) {
			this.header = header;
		}

		public MarkitResponseBuilder addValue(MarkitValue value) {
			values.add(value);
			return this;
		}

		public MarkitResponseBuilder addValues(List<MarkitValue> value) {
			values.addAll(value);
			return this;
		}

		public MarkitResponse build() {
			return new MarkitResponse(this);
		}
	}

	@Override
	public String toString() {
		return "MarkitResponse [header=" + header + ", values=" + values + "]";
	}
}
