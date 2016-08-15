package com.acuo.valuation.providers.markit.services;

import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.services.OkHttpClient;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class})
public class PortfolioValuationsRetrieverTest {

    @Rule
    public ResourceFile response = new ResourceFile("/markit/responses/markit-sample.xml");

    @Inject
    ResponseParser responseParser;

    MockWebServer server = new MockWebServer();

    PortfolioValuationsRetriever retriever;

    @Before
    public void setUp() throws Exception {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/").toString(), "username", "password", "0", "10000");

        OkHttpClient client = new MarkitClient(httpClient, markitEndPointConfig);

        retriever = new PortfolioValuationsRetriever(client, responseParser);
    }

    @Test
    public void retrieve() throws Exception {

        server.enqueue(new MockResponse().setBody(response.getContent()));

        LocalDate asOf = new LocalDateAdapter().unmarshal("2016-06-10");
        Result result = retriever.retrieve(asOf, "Test_IRS");

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(result).isNotNull();
        assertThat(body).contains("username=username");
        assertThat(body).contains("password=password");
        assertThat(body).contains("asof=2016-06-10");
        assertThat(body).contains("format=xml");
    }

}