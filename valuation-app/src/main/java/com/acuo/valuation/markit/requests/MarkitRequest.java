package com.acuo.valuation.markit.requests;

import java.util.Date;

import com.acuo.valuation.requests.Request;
import com.acuo.valuation.requests.RequestData;

public class MarkitRequest implements Request {

	private final Date valuationDate;
	private final String valuationCurrency;
	private final RequestData data;

	private MarkitRequest(RequestInput definition) {
		this.valuationDate = definition.valuationDate;
		this.valuationCurrency = definition.valuationCurrency;
		this.data = MarkitRequestData.of(definition.data);
	}

	public static Request of(RequestInput definition) {
		return new MarkitRequest(definition);
	}

	@Override
	public Date getValuationDate() {
		return valuationDate;
	}

	@Override
	public String getValuationCurrency() {
		return valuationCurrency;
	}

	@Override
	public RequestData getData() {
		return data;
	}
}