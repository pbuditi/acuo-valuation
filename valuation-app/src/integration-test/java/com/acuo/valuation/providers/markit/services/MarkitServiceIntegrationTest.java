package com.acuo.valuation.providers.markit.services;

import com.acuo.common.app.ServiceManagerModule;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.DataImporter;
import com.acuo.persist.core.DataLoader;
import com.acuo.common.model.ids.ClientId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.collect.result.Result;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationIntegrationTestModule.class,
                                MappingModule.class,
                                EncryptionModule.class,
                                Neo4jPersistModule.class,
                                ParsersModule.class,
                                EndPointModule.class,
                                ServicesModule.class,
                                ServiceManagerModule.class,
                                Neo4jIntegrationTestModule.class,
                                RepositoryModule.class,
                                DataLoaderModule.class,
                                DataImporterModule.class})
public class MarkitServiceIntegrationTest {

    @Inject
    private PricingService pricingService = null;

    @Inject
    private DataImporter dataImporter = null;

    @Inject
    private DataLoader dataLoader = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Before
    public void setUp(){
        dataLoader.purgeDatabase();
        dataImporter.importFiles("clients", "legalentities", "accounts");
        tradeUploadService.fromExcel(oneIRS.getInputStream());
    }

    /*@Test
    @Ignore
    public void testPriceASwap() {
        SwapTrade swap = SwapHelper.createTrade();
        MarkitResults pricingResults = pricingService.priceSwapTrades(ImmutableList.of(swap));

        assertThat(pricingResults).isNotNull();
        ImmutableList<Result<MarkitValuation>> results = pricingResults.getResults();
        assertThat(results).isNotEmpty();
        for (Result result: results) {
            assertThat(result.isSuccess()).isTrue();
        }
    }*/

    @Test
    public void testPriceSwapFromClientId() {
        MarkitResults markitResults = pricingService.priceTradesOf(ClientId.fromString("c1"));
        assertThat(markitResults).isNotNull();
        List<Result<MarkitValuation>> results = markitResults.getResults();
        assertThat(results).isNotEmpty();
        for (Result result: results) {
            assertThat(result.isSuccess()).isTrue();
        }
    }
}