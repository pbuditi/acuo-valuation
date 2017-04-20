package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AssetService;
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
                { "IT0001444378", "ISIN", 2.6415304243758504d, 139.524d, LocalDate.now(), "PercentCleanPrice", "Reuters",  Currency.EUR, Currency.EUR, 6d },
                { "IT0001444378", "ISIN", 2.6415304243758504d, 139.524d, LocalDate.now(), "PercentCleanPrice", "Reuters",  Currency.EUR, Currency.EUR, 6d }
        });
    }

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
    public void testPersist() {
        final List<AssetValuation> assetValuations = assetValuations();
        assetValuations.stream().forEach(assetValuation -> {
            assetsPersistService.persist(assetValuation);
            Asset asset = assetService.findById(assetValuation.getAssetId(), 2);
            final com.acuo.persist.entity.AssetValuation valuation = asset.getValuation();
            assertThat(valuation).isNotNull();
            assertThat(valuation.getValues()).hasSize(1);
        });
    }

    private static List<AssetValuation> assetValuations() {
        return stream(data().spliterator(), true)
                .map(objects -> {
                    AssetValuation assetValuation = new AssetValuation();
                    assetValuation.setAssetId((String) objects[0]);
                    assetValuation.setIdType((String) objects[1]);
                    assetValuation.setYield((Double) objects[2]);
                    assetValuation.setPrice((Double) objects[3]);
                    assetValuation.setValuationDateTime((LocalDate) objects[4]);
                    assetValuation.setPriceQuotationType((String) objects[5]);
                    assetValuation.setSource((String) objects[6]);
                    assetValuation.setNominalCurrency((Currency) objects[7]);
                    assetValuation.setReportCurrency((Currency) objects[8]);
                    assetValuation.setCoupon((Double) objects[9]);
                    return assetValuation;
                })
                .collect(toList());
    }
}
