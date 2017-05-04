package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.utils.SwapTradeBuilder;
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
public class ClarusMarginCalcServiceTest {

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-lch.json");

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Named("clarus")
    Transformer<SwapTrade> transformer;

    @Inject
    TradeService<Trade> irsService;

    @Inject
    ImportService importService;

    @Inject
    TradeUploadService tradeUploadService;

    MockWebServer server = new MockWebServer();

    ClarusMarginCalcService service;

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        ClarusEndPointConfig config = new ClarusEndPointConfig(server.url("/").toString(), "key", "secret", "10000", "false", null);

        ClientEndPoint<ClarusEndPointConfig> clientEndPoint = new OkHttpClient(httpClient, config);

        service = new ClarusMarginCalcService(clientEndPoint, objectMapper, transformer);

        importService.reload();
        tradeUploadService.uploadTradesFromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testMakeRequest() throws IOException {
        String id = "455123";
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        Trade trade = irsService.find(TradeId.fromString(id));
        if (trade != null) {
            SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS) trade);
            swapTrades.add(swapTrade);
        }

        String request = service.makeRequest(swapTrades, DataModel.LCH);
        assertThat(request).isNotNull();
        Assert.assertThat(request, isJson());
        //Assert.assertThat(request, jsonEquals(json.getContent()).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testMarginCalcOnCmePortfolioFromListOfSwaps() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(response.getContent()));
        String id = "455123";
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        Trade trade = irsService.find(TradeId.fromString(id));
        if (trade != null) {
            SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS) trade);
            swapTrades.add(swapTrade);
        }

        MarginResults results = service.send(swapTrades, DataModel.LCH, MarginCallType.VM);
        assertThat(results).isNotNull();
        assertThat(results.getResults().size()).isEqualTo(1);
    }

}