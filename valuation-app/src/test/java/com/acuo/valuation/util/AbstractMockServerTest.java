package com.acuo.valuation.util;

import com.acuo.common.util.InstanceTestClassListener;
import com.google.inject.Injector;
import okhttp3.mockwebserver.MockWebServer;

import javax.inject.Inject;
import java.io.IOException;

public class AbstractMockServerTest implements InstanceTestClassListener {

    @Inject
    private Injector injector = null;

    protected static MockWebServer server;

    @Override
    public final void beforeClassSetup() {
        server = injector.getInstance(MockWebServer.class);
    }

    @Override
    public final void afterClassSetup() {
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}