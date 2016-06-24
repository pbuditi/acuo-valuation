package com.acuo.valuation.markit.services;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.reports.ReportParser;
import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.util.SwapHelper;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.OkHttpClient;
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

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ JaxbModule.class })
public class PortfolioValuationsSenderTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile report = new ResourceFile("/reports/markit-test-01.xml");

    @Inject
    RequestParser requestParser;

    @Inject
    ReportParser reportParser;

    @Inject
    @Named("xml")
    Marshaller marshaller;

    MockWebServer server = new MockWebServer();

    PortfolioValuationsSender sender;

    @Before
    public void setUp() throws Exception {
        server.start();

        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/").toString(), "username", "password", 0l);

        MarkitClient client = new MarkitClient(httpClient, markitEndPointConfig);

        sender = new PortfolioValuationsSender(client, requestParser, reportParser);
    }

    @Test
    public void send() throws Exception {

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(report.getContent()));

        IrSwapInput swapInput = SwapHelper.irSwapInput();
        IrSwap swap = new IrSwap(swapInput);
        Report r = sender.send(swap);

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("username")
                .contains("password")
                .contains(swap.tradeId());

        IntStream.range(1, 3).forEach(i -> {
            try {
                RecordedRequest key = server.takeRequest();
                String keyBody = key.getBody().readUtf8();
                System.out.println(keyBody);
                assertThat(keyBody).contains("username=username")
                        .contains("password=password")
                        .contains("key=key")
                        .contains("version=2");
            } catch (InterruptedException e) {}
        });
        assertThat(r).isNotNull();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }


}