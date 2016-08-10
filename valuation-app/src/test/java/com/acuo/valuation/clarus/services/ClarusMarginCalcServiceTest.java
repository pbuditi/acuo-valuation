package com.acuo.valuation.clarus.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.clarus.protocol.Clarus;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.MarginCalcService;
import com.acuo.valuation.util.SwapHelper;
import com.acuo.valuation.utils.LoggingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.acuo.valuation.clarus.protocol.Clarus.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({JaxbModule.class})
public class ClarusMarginCalcServiceTest {

    @Rule
    public ResourceFile request = new ResourceFile("/clarus/request/clarus-cme.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-cme.json");

    @Inject
    ObjectMapper mapper;

    MockWebServer server = new MockWebServer();

    MarginCalcService service;

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ClarusEndPointConfig config = new ClarusEndPointConfig(server.url("/").toString(), "key", "secret", "1");

        ClientEndPoint<ClarusEndPointConfig> clientEndPoint = new ClarusClient(httpClient, config);

        service = new ClarusMarginCalcService(clientEndPoint, mapper);
    }

    @Test
    public void testMarginCalcOnCmePortfolio() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        List<? extends Result> results = service.send(cmeCsv.getContent(), DataFormat.CME, DataType.SwapRegister);

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
    }

}
