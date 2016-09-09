package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.marshal.Marshaller;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class})
public class PortfolioValuationsSenderTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Inject
    ReportParser reportParser;

    @Inject
    @Named("clarus")
    Transformer<SwapTrade> transformer;

    @Inject
    @Named("xml")
    Marshaller marshaller;

    MockWebServer server = new MockWebServer();

    PortfolioValuationsSender sender;

    List<SwapTrade> swaps;

    @Before
    public void setUp() throws Exception {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/").toString(), "username", "password", "0", "10000");

        OkHttpClient client = new MarkitClient(httpClient, markitEndPointConfig);

        sender = new PortfolioValuationsSender(client, reportParser, transformer);

        swaps = transformer.deserialiseToList(cmeCsv.getContent());
    }

    @Test
    public void send() throws Exception {

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(report.getContent()));

        Report r = sender.send(swaps);

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("username")
                .contains("password")
                /*.contains(swap.getTradeId())*/;

        IntStream.range(1, 3).forEach(i -> {
            try {
                RecordedRequest key = server.takeRequest();
                String keyBody = key.getBody().readUtf8();
                System.out.println(keyBody);
                assertThat(keyBody).contains("username=username")
                        .contains("password=password")
                        .contains("key=key")
                        .contains("version=2");
            } catch (InterruptedException e) {
            }
        });
        assertThat(r).isNotNull();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }


}