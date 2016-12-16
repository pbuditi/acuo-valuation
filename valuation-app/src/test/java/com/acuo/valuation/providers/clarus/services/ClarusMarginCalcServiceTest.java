package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.opengamma.strata.collect.result.Result;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class ClarusMarginCalcServiceTest {

    @Rule
    public ResourceFile json = new ResourceFile("/clarus/request/clarus-cme.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-cme.json");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Named("clarus")
    Transformer<SwapTrade> transformer;

    MockWebServer server = new MockWebServer();

    @Inject
    ValuationService valuationService;

    @Inject
    PortfolioService portfolioService;

    ClarusMarginCalcService service;

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ClarusEndPointConfig config = new ClarusEndPointConfig(server.url("/").toString(), "key", "secret", "10000");

        ClientEndPoint<ClarusEndPointConfig> clientEndPoint = new OkHttpClient(httpClient, config);

        service = new ClarusMarginCalcService(clientEndPoint, objectMapper, transformer, valuationService, portfolioService);
    }

    @Test
    public void testMakeRequest() throws IOException {
        List<SwapTrade> swaps = transformer.deserialiseToList(cmeCsv.getContent());
        String request = service.makeRequest(swaps, DataFormat.CME, DataType.SwapRegister);
        assertThat(request).isNotNull();
        Assert.assertThat(request, isJson());
        //Assert.assertThat(request, jsonEquals(json.getContent()).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testMarginCalcOnCmePortfolioFromListOfSwaps() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        List<SwapTrade> swaps = transformer.deserialiseToList(cmeCsv.getContent());

        MarginResults results = service.send(swaps, DataFormat.CME, DataType.SwapRegister);

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(results).isNotNull();
        assertThat(results.getResults().size()).isEqualTo(2);
    }

    @Test
    public void testSavePv()
    {
        MarginValuation marginValuation = new MarginValuation("", 1d, 1d, 1d);
        Result<MarginValuation> result = Result.success(marginValuation);
        MarginResults marginResults = MarginResults.of(Arrays.asList(result));
        marginResults.setPortfolioId("p2");
        marginResults.setValuationDate(LocalDate.now());
        Assert.assertTrue(service.savePV(marginResults));
    }

}