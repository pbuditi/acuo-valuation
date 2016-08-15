package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.services.DataMapper;
import com.acuo.common.model.IrSwap;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.utils.LoggingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.JsonMatchers.*;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class})
public class ClarusMarginCalcServiceTest {

    @Rule
    public ResourceFile json = new ResourceFile("/clarus/request/clarus-cme.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-cme.json");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DataMapper dataMapper;

    MockWebServer server = new MockWebServer();

    ClarusMarginCalcService service;

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ClarusEndPointConfig config = new ClarusEndPointConfig(server.url("/").toString(), "key", "secret", "10000");

        ClientEndPoint<ClarusEndPointConfig> clientEndPoint = new ClarusClient(httpClient, config);

        service = new ClarusMarginCalcService(clientEndPoint, objectMapper, dataMapper);
    }

    @Test
    public void testMakeRequest() throws IOException {
        List<IrSwap> swaps = dataMapper.fromCmeFile(cmeCsv.getContent());
        String request = service.makeRequest(swaps, DataFormat.CME, DataType.SwapRegister);
        assertThat(request).isNotNull();
        Assert.assertThat(request, isJson());
        Assert.assertThat(request, jsonEquals(json.getContent()).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testMarginCalcOnCmePortfolioFromListOfSwaps() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        List<IrSwap> swaps = dataMapper.fromCmeFile(cmeCsv.getContent());

        List<? extends Result> results = service.send(swaps, DataFormat.CME, DataType.SwapRegister);

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
    }

}
