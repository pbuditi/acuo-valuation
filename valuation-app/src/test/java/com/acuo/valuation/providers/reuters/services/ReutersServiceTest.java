package com.acuo.valuation.providers.reuters.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
@Slf4j
public class ReutersServiceTest {

    private ReutersService reutersService;

    MockWebServer server = new MockWebServer();

    @com.google.inject.Inject
    @Named("assets")
    Transformer<Assets> transformer;

    @com.google.inject.Inject
    @Named("assetValuation")
    Transformer<AssetValuation> valuationTransformer;

    @Inject
    private AssetService assetService;

    @Inject
    private AssetsPersistService assetsPersistService;

    @Rule
    public ResourceFile response = new ResourceFile("/reuters/response/response.json");

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

        ReutersEndPointConfig config = new ReutersEndPointConfig("http", server.getHostName(), server.getPort(), "testUpload" ,"key", "secret", "10000", 10000,"false");

        ClientEndPoint<ReutersEndPointConfig> clientEndPoint = new OkHttpClient<>(httpClient, config);

        reutersService = new ReutersServiceImpl(clientEndPoint, transformer, assetService, assetsPersistService, valuationTransformer);

    }

    @Test
    public void testSend() throws Exception
    {
        server.enqueue(new MockResponse().setBody(response.getContent()));
        Assets assets = new Assets();
        List<Assets> assetsList = new ArrayList<>();
        assetsList.add(assets);
        assets.setAssetId("IT0001444378");
        assets.setType("BOND");
        assets.setIdType("ISIN");
        assets.setName("BUONI POLIENNALI DEL TES");
        assets.setCurrency(Currency.EUR);
        assets.setICADCode("IT-BTP");
        assets.setIssueDate(LocalDate.now());
        assets.setMaturityDate(LocalDate.now());
        assets.setFitchRating("BBB+");
        assets.setParValue(1000d);
        assets.setMinUnit(1d);
        assets.setInternalCost(0.002);
        assets.setAvailableQuantities(1000);
        List<AssetValuation> assetses = reutersService.send(assetsList);
        Assert.assertTrue(assetses.size() > 0);

    }

}
