package com.acuo.valuation.util;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.providers.markit.services.MarkitClient;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitFormCall;
import com.acuo.valuation.providers.markit.services.MarkitMultipartCall;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MockServerTest {

    @Rule
    public ResourceFile request = new ResourceFile("/markit/requests/markit-sample.xml");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/p45.xml");

    @Rule
    public ResourceFile result = new ResourceFile("/markit/responses/p45.xml");

    private MockServer server;

    ClientEndPoint<MarkitEndPointConfig> client;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        new Thread(server).start();
        Thread.sleep(1000);
        MarkitEndPointConfig config = new MarkitEndPointConfig(server.getHttpUrl(), "", "", "username", "password", "0", "10000", "false");
        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        client = new MarkitClient(httpClient, config);
    }

    @Test
    public void testPostFile() throws Exception {
        String response = MarkitMultipartCall.of(client).with("theFile", this.request.getContent()).create().send();
        assertThat(response).isEqualTo("key");
    }

    @Test
    public void testPostForReport() throws Exception {
        String response = MarkitFormCall.of(client).with("key", "key")
                .with("version", "2")
                .retryWhile(s -> s.startsWith("Markit upload still processing.")).create().send();
        assertThat(response).isEqualTo(report.getContent());
    }

    @Test
    public void testPostForResult() throws Exception {
        String response = MarkitFormCall.of(client).with("asof", "2016-06-10").with("format", "xml").create().send();
        assertThat(response).isEqualTo(result.getContent());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }
}