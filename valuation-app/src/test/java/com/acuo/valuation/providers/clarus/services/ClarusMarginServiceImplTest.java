package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.builders.TradeConverter;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.services.TradeUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class ClarusMarginServiceImplTest {

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-lch.json");

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Inject
    private ObjectMapper objectMapper = null;

    @Inject
    @Named("clarus")
    private Transformer<com.acuo.common.model.trade.Trade> transformer = null;

    @Inject
    private TradeService<Trade> irsService = null;

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    private MockWebServer server = new MockWebServer();

    private ClarusMarginServiceImpl service = null;

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ClarusEndPointConfig config = new ClarusEndPointConfig(server.url("/").toString(), "key", "secret", "10000", "false", null);

        ClientEndPoint<ClarusEndPointConfig> clientEndPoint = new OkHttpClient<>(httpClient, config);

        service = new ClarusMarginServiceImpl(clientEndPoint, objectMapper, transformer);

        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testMakeRequest() throws IOException {
        String id = "455123";
        List<com.acuo.common.model.trade.Trade> trades = new ArrayList<>();
        Trade entity = irsService.find(TradeId.fromString(id));
        if (entity != null) {
            com.acuo.common.model.trade.Trade swapTrade = TradeConverter.buildTrade(entity);
            trades.add(swapTrade);
        }

        String request = service.makeRequest(trades, DataModel.LCH);
        assertThat(request).isNotNull();
        Assert.assertThat(request, isJson());
        //Assert.assertThat(request, jsonEquals(json.getContent()).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testMarginCalcOnCmePortfolioFromListOfSwaps() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));
        String id = "455123";
        List<com.acuo.common.model.trade.Trade> trades = new ArrayList<>();
        Trade entity = irsService.find(TradeId.fromString(id));
        if (entity != null) {
            com.acuo.common.model.trade.Trade trade = TradeConverter.buildTrade(entity);
            trades.add(trade);
        }

        MarginResults results = service.send(trades, DataModel.LCH, MarginCallType.VM);
        assertThat(results).isNotNull();
        assertThat(results.getResults().size()).isEqualTo(1);
    }

}