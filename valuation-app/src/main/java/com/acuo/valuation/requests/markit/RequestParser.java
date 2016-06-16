package com.acuo.valuation.requests.markit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.requests.Request;

public class RequestParser {

	private static final Logger log = LoggerFactory.getLogger(RequestParser.class);

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
