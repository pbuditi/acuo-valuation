package com.acuo.valuation.modules;

import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.google.inject.AbstractModule;

public class ParsersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReportParser.class);
        bind(ResponseParser.class);
    }
}
