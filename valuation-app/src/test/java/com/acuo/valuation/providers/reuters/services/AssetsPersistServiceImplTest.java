package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.ids.AssetId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.AssetValuationService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.opengamma.strata.basics.currency.Currency;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
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
public class AssetsPersistServiceImplTest {

    private static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "IT0001444378", "ISIN", 2.6415304243758504d, 139_000_000.524d, 1_000_000.0d, LocalDateTime.now(), "PercentCleanPrice", "Reuters",  Currency.EUR, Currency.EUR, 6d },
                { "IT0001444378", "ISIN", 2.6415304243758504d, 139_000_000.524d, 1_000_000.0d, LocalDateTime.now(), "PercentCleanPrice", "Reuters",  Currency.EUR, Currency.EUR, 6d }
        });
    }

    @Inject
    ImportService importService;

    @Inject
    AssetValuationService assetsPersistService;

    @Inject
    ValuationService valuationService;

    @Inject
    AssetService assetService;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
    }

    @Test
    public void testPersist() {
        final List<AssetValuation> assetValuations = assetValuations();
        assetValuations.stream().forEach(assetValuation -> {
            assetsPersistService.persist(assetValuation);
            Asset asset = assetService.find(assetValuation.getAssetId(), 2);
            final com.acuo.persist.entity.AssetValuation valuation = valuationService.getAssetValuationFor(AssetId.fromString(asset.getAssetId()));
            assertThat(valuation).isNotNull();
            assertThat(valuation.getValues()).hasSize(1);
        });
    }

    private static List<AssetValuation> assetValuations() {
        return stream(data().spliterator(), true)
                .map(objects -> {
                    int i = 0;
                    AssetValuation assetValuation = new AssetValuation();
                    assetValuation.setAssetId((String) objects[i++]);
                    assetValuation.setIdType((String) objects[i++]);
                    assetValuation.setYield((Double) objects[i++]);
                    assetValuation.setCleanMarketValue((Double) objects[i++]);
                    assetValuation.setNotional((Double) objects[i++]);
                    assetValuation.setValuationDateTime((LocalDateTime) objects[i++]);
                    assetValuation.setPriceQuotationType((String) objects[i++]);
                    assetValuation.setSource((String) objects[i++]);
                    assetValuation.setNominalCurrency((Currency) objects[i++]);
                    assetValuation.setReportCurrency((Currency) objects[i++]);
                    assetValuation.setCoupon((Double) objects[i++]);
                    return assetValuation;
                })
                .collect(toList());
    }
}
