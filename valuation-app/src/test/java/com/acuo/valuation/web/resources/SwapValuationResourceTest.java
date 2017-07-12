package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.InstanceTestClassListener;
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
import com.acuo.valuation.providers.acuo.trades.TradeUploadServiceTransformer;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.util.MockServiceModule;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.google.inject.Injector;
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
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MockServiceModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
@Slf4j
public class SwapValuationResourceTest implements WithResteasyFixtures, InstanceTestClassListener {

    @Rule
    public ResourceFile one = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile all = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Rule
    public ResourceFile jsonRequest = new ResourceFile("/json/swaps/swap-request.json");

    @Rule
    public ResourceFile jsonPortfolioRequest = new ResourceFile("/json/swaps/swap-portfolio-request.json");

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

    @Rule
    public ResourceFile generateResponse = new ResourceFile("/json/calls/calls-all-response.json");

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeUploadServiceTransformer tradeUploadServiceTransformer = null;

    @Inject
    private Injector injector = null;

    private static MockWebServer server;

    private Dispatcher dispatcher;

    @Inject
    private SwapValuationResource resource = null;

    @Inject
    private ImportService importService = null;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
        importService.reload();
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
        tradeUploadService.fromExcel(one.createInputStream());

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
        server.enqueue(new MockResponse().setBody(clarusResponse.getContent()));
        server.enqueue(new MockResponse().setBody(clarusResponse.getContent()));

        MockHttpRequest request = MockHttpRequest.get("/swaps/price/allBilateralIRS");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        final String json = response.getContentAsString();
        assertThat(json).isNotNull();
        assertThatJson(json).isEqualTo(generateResponse.getContent());
    }

    @Test
    public void testPricePortfolios() throws URISyntaxException, IOException {
        tradeUploadServiceTransformer.fromExcel(all.createInputStream());

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));

        MockHttpRequest request = MockHttpRequest.post("/swaps/price/portfolios");
        MockHttpResponse response = new MockHttpResponse();

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(jsonPortfolioRequest.getInputStream());

        dispatcher.invoke(request, response);

        String res = response.getContentAsString();
        assertNotNull(res);
    }

    @Test
    public void testStress() {
        IntStream.range(1, 10).forEach (x -> {
            try {
                testValuationAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void beforeClassSetup() {
        server = injector.getInstance(MockWebServer.class);
    }

    @Override
    public void afterClassSetup() {
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}