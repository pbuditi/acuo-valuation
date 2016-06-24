package com.acuo.valuation.markit.services;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.OkHttpClient;
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

public class MarkitClientTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile request = new ResourceFile("/requests/markit-sample.xml");

    @Rule
    public ResourceFile report = new ResourceFile("/reports/markit-test-01.xml");

    @Rule
    public ResourceFile response = new ResourceFile("/responses/markit-sample.xml");

    MarkitEndPointConfig markitEndPointConfig;
    OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
    MockWebServer server = new MockWebServer();

    ClientEndPoint client;

    @Before
    public void setup() throws IOException {
        server.start();

        String url = server.url("/").toString();
        markitEndPointConfig = new MarkitEndPointConfig(url, "username", "password", 0l);

        client = new MarkitClient(httpClient, markitEndPointConfig);
    }

    @Test
    public void testPostFile() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("key"));

        String response = client.post().with("theFile", this.request.getContent()).send();

        assertThat(response).isNotNull();

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(body).contains("username")
                        .contains("password")
                        .contains(this.request.getContent());
    }

    @Test
    public void testSendStringWithRetry() throws IOException, InterruptedException {

        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(report.getContent()));

        String response = client.get().with("key","key")
                                      .with("version","2")
                                      .retryUntil(s -> s.startsWith(STILL_PROCESSING_KEY)).send();

        assertThat(response).isNotNull();
        IntStream.range(1, 3).forEach(i -> {
            try {
                RecordedRequest request = server.takeRequest();
                String body = request.getBody().readUtf8();
                assertThat(body).contains("username=" + markitEndPointConfig.username())
                    .contains("password=" + markitEndPointConfig.password())
                    .contains("key=key")
                    .contains("version=2");
            } catch (InterruptedException e) {}
        });
    }

    @Test
    public void testSendString() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        String asOfDate = "2016-06-10";
        String response = client.get().with("asof",asOfDate).with("format","xml").send();

        assertThat(response).isNotNull();

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("username="+markitEndPointConfig.username());
        assertThat(body).contains("password="+markitEndPointConfig.password());
        assertThat(body).contains("asof="+asOfDate);
        assertThat(body).contains("format=xml");
    }

    @After
    public void shutdown() throws IOException {
        server.shutdown();
    }

}
