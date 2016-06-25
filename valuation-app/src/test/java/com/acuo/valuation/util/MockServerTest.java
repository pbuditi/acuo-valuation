package com.acuo.valuation.util;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.services.MarkitClient;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.OkHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MockServerTest {

    @Rule
    public ResourceFile request = new ResourceFile("/requests/markit-sample.xml");

    @Rule
    public ResourceFile report = new ResourceFile("/reports/markit-test-01.xml");

    @Rule
    public ResourceFile result = new ResourceFile("/responses/markit-test-01.xml");

    MockServer server;

    ClientEndPoint client;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        new Thread(server).start();

        MarkitEndPointConfig config = new MarkitEndPointConfig("http://localhost:8080/", "username", "password", 0l);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        client = new MarkitClient(httpClient, config);
    }

    @Test
    public void testPostFile() throws Exception {
        String response = client.post().with("theFile", this.request.getContent()).send();
        assertThat(response).isEqualTo("key");
    }

    @Test
    public void testPostForReport() throws Exception {
        String response = client.get().with("key", "key")
                .with("version", "2")
                .retryUntil(s -> s.startsWith("Markit upload still processing.")).send();
        assertThat(response).isEqualTo(report.getContent());
    }

    @Test
    public void testPostForResult() throws Exception {
        String response = client.get().with("asof", "2016-06-10").with("format", "xml").send();
        assertThat(response).isEqualTo(result.getContent());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }
}