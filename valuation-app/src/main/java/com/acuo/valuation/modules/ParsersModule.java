package com.acuo.valuation.modules;

import com.acuo.valuation.markit.reports.ReportParser;
import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.markit.responses.ResponseParser;
import com.google.inject.AbstractModule;

public class ParsersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReportParser.class);
        bind(RequestParser.class);
        bind(ResponseParser.class);
    }
}
