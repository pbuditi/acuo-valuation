package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.modules.*;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.*;
import org.junit.runner.RunWith;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MarginCallResourceTest.MockServiceModule.class,
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
public class MarginCallResourceTest implements WithResteasyFixtures {

    private static MockWebServer server;
    private Dispatcher dispatcher;

    @Rule
    public ResourceFile one = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    @Rule
    public ResourceFile generateRequest = new ResourceFile("/json/calls/trades-request.json");

    @Rule
    public ResourceFile generateResponse = new ResourceFile("/json/calls/calls-response.json");

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

    @Inject
    ImportService importService;

    @Inject
    MarginCallResource resource;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    TradeCacheService cacheService;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
        setMockMarkitResponse();
        importService.reload();
        tradeUploadService.uploadTradesFromExcel(one.createInputStream());
    }

    private void setMockMarkitResponse() throws IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
    }

    @Test
    public void testGenerate() throws Exception {
        String tnxId = cacheService.put(ImmutableList.of("455820"));
        MockHttpRequest request = MockHttpRequest.get("/calls/generate/"+tnxId);
        request.contentType(MediaType.APPLICATION_JSON);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
        String json = response.getContentAsString();
        Assert.assertThat(json, isJson());
        assertEquals(generateResponse.getContent(), json);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        server.shutdown();
    }
}