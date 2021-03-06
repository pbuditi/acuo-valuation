package com.acuo.valuation.providers.markit.protocol.responses;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.protocol.responses.Response;

import javax.inject.Inject;
import javax.inject.Named;

public class ResponseParser {

    private final Marshaller marshaller;

    @Inject
    public ResponseParser(@Named("xml") Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public Response parse(String xmlData) throws Exception {
        ArgChecker.notNull(xmlData, "xmlData");
        ResponseInput def = marshaller.unmarshal(xmlData, ResponseInput.class);
        Response response = def.response();
        return response;
    }

    public String parse(Response response) throws Exception {
        ArgChecker.notNull(response, "report");
        ResponseInput definition = ResponseInput.definition(response);
        return marshaller.marshal(definition);
    }
}