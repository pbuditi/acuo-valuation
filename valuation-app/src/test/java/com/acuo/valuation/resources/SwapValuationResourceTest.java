package com.acuo.valuation.resources;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.valuation.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.web.MOXyCustomJsonProvider;
import com.google.inject.AbstractModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({SwapValuationResourceTest.MockServiceModule.class, JaxbModule.class, EndPointModule.class, ServicesModule.class, ResourcesModule.class})
public class SwapValuationResourceTest implements WithResteasyFixtures {

    @Rule
    public ResourceFile swap = new ResourceFile("/markit/requests/dto-swap-test-01.json");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Rule
    public ResourceFile response = new ResourceFile("/markit/responses/markit-test-01.xml");

    private static MockWebServer server;

    public static class MockServiceModule extends AbstractModule {
        @Override
        protected void configure() {
            server = new MockWebServer();
            MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/").toString(),
                    "username", "password", "0", "1");
            ClarusEndPointConfig clarusEndPointConfig = new ClarusEndPointConfig("host", "key", "api", "1");
            bind(MarkitEndPointConfig.class).toInstance(markitEndPointConfig);
            bind(ClarusEndPointConfig.class).toInstance(clarusEndPointConfig);
        }

    }

    Dispatcher dispatcher;

    @Inject
    SwapValuationResource resource;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(MOXyCustomJsonProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
    }

    @Test
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
        request.content(swap.getInputStream());

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        String actual = response.getContentAsString();
        assertThat(actual).isNotNull();
        assertThatJson(actual).isEqualTo("{\"pv\":-20277.510590907827}");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        server.shutdown();
    }
}
