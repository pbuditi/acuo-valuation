package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class SwapValuationResourceTest implements WithResteasyFixtures {

    @Rule
    public ResourceFile one = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile jsonRequest = new ResourceFile("/json/swaps/swap-request.json");

    @Rule
    public ResourceFile jsonResponse = new ResourceFile("/json/swaps/swap-response.json");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Rule
    public ResourceFile response = new ResourceFile("/markit/responses/markit-test-01.xml");

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    @Rule
    public ResourceFile clarusResponse = new ResourceFile("/clarus/response/clarus-lch.json");

    @Inject
    TradeUploadService tradeUploadService;

    private static MockWebServer server = new MockWebServer();

    private Dispatcher dispatcher;

    @Inject
    SwapValuationResource resource;

    @Inject
    ImportService importService;

    @BeforeClass
    public static void startServer() throws IOException {
        server.start(8282);
    }

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
        importService.reload();
    }

    private void setMockMarkitResponse() throws IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
        server.enqueue(new MockResponse().setBody(clarusResponse.getContent()));
    }

    @Test
    @Ignore
    public void testWelcomPage() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get("/swaps");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    @Test
    public void testSwapValuation() throws URISyntaxException, IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(report.getContent()));
        server.enqueue(new MockResponse().setBody(response.getContent()));

        MockHttpRequest request = MockHttpRequest.post("/swaps/value");
        MockHttpResponse response = new MockHttpResponse();

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(jsonRequest.getInputStream());

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        String actual = response.getContentAsString();
        assertThat(actual).isNotNull();
        assertThatJson(actual).isEqualTo(jsonResponse.getContent());
    }

    @Test
    public void testValuationAll() throws URISyntaxException, IOException {
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());

        setMockMarkitResponse();

        MockHttpRequest request = MockHttpRequest.get("/swaps/priceSwapTrades/allBilateralIRS");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
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

    @AfterClass
    public static void tearDown() throws IOException {
        server.shutdown();
    }
}