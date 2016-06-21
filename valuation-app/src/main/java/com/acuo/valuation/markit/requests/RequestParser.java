package com.acuo.valuation.markit.requests;

import javax.inject.Inject;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.requests.Request;

public class RequestParser {

	private final Marshaller marshaller;

	@Inject
	public RequestParser(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Request parse(String xmlData) throws Exception {
		ArgChecker.notNull(xmlData, "xmlData");
		RequestInput definition = marshaller.unmarshal(xmlData, RequestInput.class);
		return definition.request();
	}

	public String parse(Request request) throws Exception {
		ArgChecker.notNull(request, "request");
		RequestInput definition = RequestInput.definition(request);
		return marshaller.marshal(definition);
	}

}
