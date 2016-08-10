package com.acuo.valuation.providers.markit.services;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpClientTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile request = new ResourceFile("/markit/requests/markit-sample.xml");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Rule
    public ResourceFile response = new ResourceFile("/markit/responses/markit-sample.xml");

    MarkitEndPointConfig markitEndPointConfig;
    okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
    MockWebServer server = new MockWebServer();

    ClientEndPoint client;

    @Before
    public void setup() throws IOException {
        server.start();

        String url = server.url("/").toString();
        markitEndPointConfig = new MarkitEndPointConfig(url, "username", "password", "0", "150");

        client = new MarkitClient(httpClient, markitEndPointConfig);
    }

    @Test
    public void testPostFile() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("key"));

        String content = this.request.getContent();
        String response = MarkitMultipartCall.of(client)
                                                     .with("theFile", content)
                                                     .create()
                                                     .send();

        assertThat(response).isNotNull();

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(body).contains("username")
                .contains("password")
                .contains(content);
    }

    @Test
    public void testSendStringWithRetry() throws IOException, InterruptedException {

        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(report.getContent()));

        String response = MarkitFormCall.of(client)
                                                    .with("key", "key")
                                                    .with("version", "2")
                                                    .retryWhile(s -> s.startsWith(STILL_PROCESSING_KEY))
                                                    .create()
                                                    .send();

        assertThat(response).isNotNull();
        IntStream.range(1, 3).forEach(i -> {
            try {
                RecordedRequest request = server.takeRequest();
                String body = request.getBody().readUtf8();
                assertThat(body).contains("username=" + markitEndPointConfig.getUsername())
                        .contains("password=" + markitEndPointConfig.getPassword())
                        .contains("key=key")
                        .contains("version=2");
            } catch (InterruptedException e) {
            }
        });
    }

    @Test
    public void testSendString() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        String asOfDate = "2016-06-10";
        String response = MarkitFormCall.of(client)
                                                    .with("asof", asOfDate)
                                                    .with("format", "xml")
                                                    .create()
                                                    .send();

        assertThat(response).isNotNull();

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("username=" + markitEndPointConfig.getUsername());
        assertThat(body).contains("password=" + markitEndPointConfig.getPassword());
        assertThat(body).contains("asof=" + asOfDate);
        assertThat(body).contains("format=xml");
    }

    @After
    public void shutdown() throws IOException {
        server.shutdown();
    }

}
