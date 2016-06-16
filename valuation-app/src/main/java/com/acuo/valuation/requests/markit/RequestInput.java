package com.acuo.valuation.requests.markit;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.common.marshal.jaxb.DateAdapter;
import com.acuo.valuation.requests.Request;

@XmlRootElement(name = "presentvalue")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestInput {

	public RequestInput() {
	}

	private RequestInput(Request request) {
		this.valuationDate = request.getValuationDate();
		this.valuationCurrency = request.getValuationCurrency();
		this.data = new RequestDataInput(request.getData());
	}

	public static RequestInput definition(Request request) {
		return new RequestInput(request);
	}

	public Request request() {
		return MarkitRequest.of(this);
	}

	@XmlPath("valuationdate/text()")
	@XmlJavaTypeAdapter(DateAdapter.class)
	Date valuationDate;

	@XmlPath("valuationcurrency/text()")
	String valuationCurrency;

	@XmlElement(name = "data", required = true)
	public RequestDataInput data;
}
