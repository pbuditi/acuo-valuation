package com.acuo.valuation.web.resources;

import com.acuo.common.model.margin.Types;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.LocalDateUtils;
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
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.util.AbstractMockServerTest;
import com.acuo.valuation.util.MockQueueServerModule;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.collect.result.ValueWithFailures;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MockQueueServerModule.class,
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
public class MarginCallResourceTest extends AbstractMockServerTest implements WithResteasyFixtures {

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

    @Rule
    public ResourceFile jsonPortfolioRequest = new ResourceFile("/json/calls/portfolio-request.json");

    @Rule
    public ResourceFile jsonGenPortfolioResponse = new ResourceFile("/json/calls/gen-portfolio-response.json");

    @Rule
    public ResourceFile jsonSplitPortfolioResponse = new ResourceFile("/json/calls/split-portfolio-response.json");

    @Rule
    public ResourceFile clarusResponse = new ResourceFile("/clarus/response/clarus-lch.json");

    @Inject
    private ImportService importService = null;

    @Inject
    private MarginCallResource resource = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeCacheService cacheService = null;

    @Inject
    private ResultPersister<MarkitResults> markitPersister = null;

    @Inject
    private ResultPersister<MarginResults> marginPersister = null;

    @Mock
    private MarkitResults markitResults;

    @Mock
    private MarginResults vmResults;

    @Mock
    private MarginResults imResults;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);

        importService.reload();
        tradeUploadService.fromExcel(one.createInputStream());

        mockConditions();

        markitPersister.persist(markitResults);
        marginPersister.persist(vmResults);
        marginPersister.persist(imResults);
    }

    private void setMockMarkitResponse() throws IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
    }

    @Test
    public void testGenerate() throws Exception {
        setMockMarkitResponse();

        String tnxId = cacheService.put(ImmutableList.of("455820"));
        MockHttpRequest request = MockHttpRequest.get("/calls/generate/"+tnxId);
        request.contentType(MediaType.APPLICATION_JSON);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
        String json = response.getContentAsString();
        Assert.assertThat(json, isJson());
        assertThatJson(json).isEqualTo(generateResponse.getContent());
    }

    @Test
    public void testGeneratePortfolios() throws URISyntaxException, IOException {
        setMockMarkitResponse();

        MockHttpRequest request = MockHttpRequest.post("/calls/generate/portfolios");
        MockHttpResponse response = new MockHttpResponse();

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(jsonPortfolioRequest.getInputStream());

        dispatcher.invoke(request, response);

        String json = response.getContentAsString();
        assertNotNull(json);
        Assert.assertThat(json, isJson());
        assertThatJson(json).isEqualTo(jsonGenPortfolioResponse.getContent());
    }

    @Test
    @Ignore
    public void testSplitPortfolios() throws URISyntaxException, IOException {

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
        server.enqueue(new MockResponse().setBody(clarusResponse.getContent()));
        server.enqueue(new MockResponse().setBody(clarusResponse.getContent()));

        MockHttpRequest request = MockHttpRequest.post("/calls/split/portfolios");
        MockHttpResponse response = new MockHttpResponse();

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(jsonPortfolioRequest.getInputStream());

        dispatcher.invoke(request, response);

        String json = response.getContentAsString();
        assertNotNull(json);
        Assert.assertThat(json, isJson());
        assertThatJson(json).isEqualTo(jsonSplitPortfolioResponse.getContent());
    }

    private void mockConditions() {
        MarginValuation vmValuation = new MarginValuation("test",
                10.0d,
                10.0d,
                10.0d,
                Types.CallType.Variation,
                "p31");
        when(vmResults.getResults()).thenReturn(com.google.common.collect.ImmutableList.of(Result.success(vmValuation)));
        when(vmResults.getMarginType()).thenReturn(Types.CallType.Variation);
        when(vmResults.getValuationDate()).thenReturn(LocalDateUtils.minus(LocalDate.now(), 1));
        when(vmResults.getCurrency()).thenReturn(Currency.USD.getCode());

        MarginValuation imValuation = new MarginValuation("test",
                100.0d,
                100.0d,
                100.0d,
                Types.CallType.Initial,
                "p31");
        when(imResults.getResults()).thenReturn(com.google.common.collect.ImmutableList.of(Result.success(imValuation)));
        when(imResults.getMarginType()).thenReturn(Types.CallType.Initial);
        when(imResults.getValuationDate()).thenReturn(LocalDateUtils.minus(LocalDate.now(), 1));
        when(imResults.getCurrency()).thenReturn(Currency.USD.getCode());

        MarkitValuation markitValuation = new MarkitValuation("455820", ValueWithFailures.of(10.0d));
        when(markitResults.getResults()).thenReturn(com.google.common.collect.ImmutableList.of(Result.success(markitValuation)));
        when(markitResults.getValuationDate()).thenReturn(LocalDateUtils.minus(LocalDate.now(), 1));
        when(markitResults.getCurrency()).thenReturn(Currency.USD);
    }
}