package com.acuo.valuation.markit.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.reports.ReportParser;
import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.util.SwapHelper;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ JaxbModule.class })
public class PortfolioValuationsSenderTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile report = new ResourceFile("/reports/markit-sample.xml");

    @Inject
    RequestParser requestParser;

    @Inject
    ReportParser reportParser;

    MarkitEndPointConfig markitEndPointConfig;
    OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
    MockWebServer server = new MockWebServer();

    PortfolioValuationsSender sender;

    @Before
    public void setUp() throws Exception {
        server.start();
        String url = server.url("/").toString();
        markitEndPointConfig = new MarkitEndPointConfig(url, "username", "password", 0l);

        sender = new PortfolioValuationsSender(markitEndPointConfig, requestParser, reportParser);
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

        assertThat(r).isNotNull();
        //assertThat(reportParser.parse(r)).isEqualTo(report.getContent());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }


}