package com.acuo.valuation.module;

import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.EndPointConfig;
import com.google.inject.AbstractModule;
import okhttp3.mockwebserver.MockWebServer;

public class MockServerModule extends AbstractModule {

    MockWebServer server = new MockWebServer();

    @Override
    protected void configure() {
        bind(MockWebServer.class).toInstance(server);
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/").toString(),
                "username", "password", 0l);
        bind(EndPointConfig.class).toInstance(markitEndPointConfig);
    }
}
