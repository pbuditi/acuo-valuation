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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @Test
    public void testValuationAll() throws URISyntaxException, IOException {
        TradeUploadService tradeUploadService = new TradeUploadServiceImpl(accountService, portfolioService, tradeService);
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));

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
    @Ignore
    public void testStress() {
        while (true) {
            try {
                testValuationAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Ignore
    public void testUploadExcelFile2() throws URISyntaxException, IOException {
        MockHttpRequest request = MockHttpRequest.post("/upload");
        request.contentType(MediaType.APPLICATION_OCTET_STREAM);
        byte[] bytes = excel.getContent().getBytes();
        request.content(excel.getInputStream());
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    @Test
    @Ignore
    public void testUploadExcelFile() throws URISyntaxException, IOException {
        Map parts = new HashMap();
        parts.put("file", excel.getContent());
        MockHttpRequest request = multipartRequest("/upload", parts);

        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    /**
     * Return a multipart/form-data MockHttpRequest
     *
     * @param parts Key is the name of the part, value is either a String or a File.
     * @return
     */
    private MockHttpRequest multipartRequest(String uri, Map parts) throws URISyntaxException, IOException {
        MockHttpRequest req = MockHttpRequest.post(uri);
        String boundary = UUID.randomUUID().toString();
        req.contentType("multipart/form-data; boundary=" + boundary);

        //File tmpMultipartFile = Files.createTempFile(null, null).toFile();
        //System.out.println("Tmp file:" + tmpMultipartFile.getAbsolutePath());
        //FileWriter writer = new FileWriter(tmpMultipartFile);
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
        log.debug(body);
        byte[] bytes = body.getBytes("UTF-8");
        req.content(bytes);
        return req;
    }

}
