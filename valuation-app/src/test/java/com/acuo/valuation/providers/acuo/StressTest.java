package com.acuo.valuation.providers.acuo;


import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.SwapService;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.acuo.valuation.web.resources.SwapValuationResource;
import com.acuo.valuation.web.resources.SwapValuationResourceTest;
import com.google.inject.AbstractModule;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        StressTest.MockServiceModule.class,
        Neo4jPersistModule.class,
        DataImporterModule.class,
        DataLoaderModule.class,
        ImportServiceModule.class,
        MappingModule.class,
        EndPointModule.class,
        RepositoryModule.class,
        ServicesModule.class,
        ResourcesModule.class})
public class StressTest implements WithResteasyFixtures {

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");


    TradeUploadServiceImpl service;

    @Inject
    TradeService<IRS> irsService;

    @Inject
    TradeService<FRA> fraService;

    @Inject
    TradingAccountService accountService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ImportService importService;

    @Mock
    SwapService swapService;

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

    Dispatcher dispatcher;

    @Inject
    SwapValuationResource resource;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
        service = new TradeUploadServiceImpl(irsService, fraService, accountService, portfolioService, swapService);

    }

    @Ignore
    @Test
    public void testValuationAll() throws URISyntaxException, IOException {
        importService.reload();
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
        when(swapService.price(any(List.class))).thenReturn(new MarginCallDetail());
        service.uploadTradesFromExcel(excel.createInputStream());

        MockHttpRequest request = MockHttpRequest.get("/swaps/price/allBilateralIRS");
        MockHttpResponse response = new MockHttpResponse();


        dispatcher.invoke(request, response);

    }

    @Ignore
    @Test
    public void testStress()
    {
        while(true)
        {
            try {
                testValuationAll();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
