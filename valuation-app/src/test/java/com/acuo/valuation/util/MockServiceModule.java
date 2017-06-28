package com.acuo.valuation.util;

import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.google.inject.AbstractModule;
import okhttp3.mockwebserver.MockWebServer;

public class MockServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        MockWebServer server = new MockWebServer();
        bind(MockWebServer.class).toInstance(server);
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/"), "", "",
                "username", "password", "0", "10000", "false");
        ClarusEndPointConfig clarusEndPointConfig = new ClarusEndPointConfig(server.url("/").url().toString(), "key", "api", "10000", "false", null);
        bind(MarkitEndPointConfig.class).toInstance(markitEndPointConfig);
        bind(ClarusEndPointConfig.class).toInstance(clarusEndPointConfig);
    }
}