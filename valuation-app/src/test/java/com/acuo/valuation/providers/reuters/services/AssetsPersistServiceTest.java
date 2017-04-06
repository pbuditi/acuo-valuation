package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.opengamma.strata.basics.currency.Currency;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;

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
public class AssetsPersistServiceTest {

    @Inject
    ImportService importService;

    @Inject
    AssetsPersistService assetsPersistService;

    @Inject
    AssetService assetService;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
    }

    @Test
    public void testPersist()
    {
        AssetValuation assetValuation = new AssetValuation();
        assetValuation.setAssetId("IT0001444378");
        assetValuation.setIdType("ISIN");

        assetValuation.setYield(2.6415304243758504);
        assetValuation.setPrice(139.524);
        assetValuation.setValuationDateTime(LocalDate.now());
        assetValuation.setPriceQuotationType("PercentCleanPrice");
        assetValuation.setSource("Reuters");
        assetValuation.setNominalCurrency(Currency.EUR);
        assetValuation.setReportCurrency(Currency.EUR);
        assetValuation.setCoupon(6);
        assetsPersistService.persist(assetValuation);

        Asset asset = assetService.findById("IT0001444378", 2);

        Valuation valuation = asset.getValuation();
        Assert.assertNotNull(valuation);

        Assert.assertTrue(valuation.getValues().size() > 0);

    }
}
