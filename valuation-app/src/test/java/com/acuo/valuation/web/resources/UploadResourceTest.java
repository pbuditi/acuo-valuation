package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.providers.acuo.TradeUploadServiceImpl;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        UploadResourceTest.MockServiceModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataImporterModule.class,
        DataLoaderModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        MappingModule.class,
        EndPointModule.class,
        ServicesModule.class,
        ResourcesModule.class})
@Slf4j
public class UploadResourceTest implements WithResteasyFixtures {

    Dispatcher dispatcher;

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Rule
    public ResourceFile one = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile generatedllMC = new ResourceFile("/acuo/margincalls/upload-all-mc.json");

    @Inject
    ImportService importService;

    @Inject
    UploadResource uploadResource;

    @Inject
    SwapValuationResource swapValuationResource;

    @Inject
    TradingAccountService accountService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    TradeService<Trade> tradeService;

    private static MockWebServer server;

    public static class MockServiceModule extends AbstractModule {
        @Override
        protected void configure() {
            server = new MockWebServer();
            MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/"), "", "",
                    "username", "password", "0", "10000", "false");
            ClarusEndPointConfig clarusEndPointConfig = new ClarusEndPointConfig("host", "key", "api", "10000", "false");
            bind(MarkitEndPointConfig.class).toInstance(markitEndPointConfig);
            bind(ClarusEndPointConfig.class).toInstance(clarusEndPointConfig);
        }

    }

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(uploadResource);
        dispatcher.getRegistry().addSingletonResource(swapValuationResource);
        importService.reload();
    }

    private void setMockMarkitResponse() throws IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
    }

    @Test
    public void testValuationAll() throws URISyntaxException, IOException {
        TradeUploadService tradeUploadService = new TradeUploadServiceImpl(accountService, portfolioService, tradeService);
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());

        setMockMarkitResponse();

        MockHttpRequest request = MockHttpRequest.get("/swaps/priceSwapTrades/allBilateralIRS");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    @Test
    public void testOne() {
        TradeUploadService tradeUploadService = new TradeUploadServiceImpl(accountService, portfolioService, tradeService);
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());
    }

    @Test
    public void testStress() {
        IntStream.range(1, 10).forEach (x -> {
            try {
                setMockMarkitResponse();
                testValuationAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testUploadBigFile() throws URISyntaxException, IOException {
        testFileUpload(excel);
    }

    @Test
    public void testUploadSmallFileMultipleTimes() throws URISyntaxException, IOException {
        testFileUpload(one);
        testFileUpload(one);
    }

    private void testFileUpload(ResourceFile resourceFile) throws IOException, URISyntaxException {
        setMockMarkitResponse();

        Map parts = new HashMap();
        String charSet = "ISO-8859-1";
        parts.put("file", resourceFile.getContent(charSet));
        MockHttpRequest request = multipartRequest("/upload", parts, charSet);

        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString())
                .isNotNull();
        //.isEqualTo(generatedllMC.getContent());
    }


    /**
     * Return a multipart/form-data MockHttpRequest
     *
     * @param parts Key is the name of the part, value is either a String or a File.
     * @return
     */
    private MockHttpRequest multipartRequest(String uri, Map parts, String charSet) throws URISyntaxException, IOException {
        MockHttpRequest req = MockHttpRequest.post(uri);
        String boundary = UUID.randomUUID().toString();
        req.contentType("multipart/form-data; boundary=" + boundary);

        StringWriter writer = new StringWriter();
        writer.append("--").append(boundary);

        Set<Map.Entry> set = parts.entrySet();
        for (Map.Entry entry : set) {
            if (entry.getValue() instanceof String) {
                writer.append("\n");
                writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append("\n\n");
                writer.append(entry.getValue().toString()).append("\n");
                writer.append("--").append(boundary);
            } else if (entry.getValue() instanceof InputStream) {
                writer.append("\n");
                InputStream val = (InputStream) entry.getValue();
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"file.bin\"").append("\n");
                writer.append("Content-Type: application/octet-stream").append("\n\n");
                int b = val.read();
                while (b >= 0) {
                    writer.write(b);
                    writer.flush();
                    b = val.read();
                }
                writer.append("\n").append("--").append(boundary);
            }
        }
        writer.append("--");
        writer.flush();
        writer.close();
        String body = writer.toString();
        byte[] bytes = body.getBytes(charSet);
        req.content(bytes);
        return req;
    }
}
