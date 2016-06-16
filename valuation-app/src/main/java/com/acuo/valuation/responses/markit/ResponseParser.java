package com.acuo.valuation.responses.markit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.responses.Response;

public class ResponseParser {

	private static final Logger log = LoggerFactory.getLogger(ResponseParser.class);

	private final Marshaller marshaller;

	@Inject
	public ResponseParser(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Response parse(String xmlData) throws Exception {
		ArgChecker.notNull(xmlData, "xmlData");
		ResponseDefinition def = marshaller.unmarshal(xmlData, ResponseDefinition.class);
		Response response = def.response();
		return response;
	}

	public String parse(Response response) throws Exception {
		ArgChecker.notNull(response, "report");
		ResponseDefinition definition = ResponseDefinition.definition(response);
		return marshaller.marshal(definition);
	}
}