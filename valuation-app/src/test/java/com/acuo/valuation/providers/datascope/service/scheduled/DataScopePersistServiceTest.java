package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.offset;

@Slf4j
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class DataScopePersistServiceTest {

    @Inject
    DataScopePersistService dataScopePersistService;

    @Inject
    ImportService importService;

    @Inject
    CurrencyService currencyService;

    @Inject
    AssetService assetService;

    @Before
    public void setup() throws IOException {
        importService.reload();
    }

    @Test
    public void testPersistFxRate()
    {
        List<String> lines = new ArrayList<>();
        lines.add("BHDUSD=R,2.65252,04/17/2017 20:52:03");
        lines.add("JPYUSD=R,0.88,04/17/2017 20:52:03");
        lines.add("BMDUSD=R,,04/17/2017 21:02:00");
        dataScopePersistService.persistFxRate(lines);

        final Double bhd = currencyService.getFXValue(Currency.of("BHD"));
        assertThat(bhd).isNotNull().isEqualTo(2.65252, offset(0.00000001));

        final Double jpy = currencyService.getFXValue(Currency.of("JPY"));
        assertThat(jpy).isNotNull().isEqualTo(0.0088, offset(0.00000001));

        final Map<Currency, Double> allFX = currencyService.getAllFX();
        assertThat(allFX).isNotNull().isNotEmpty();
        assertThat(allFX.keySet()).hasSize(3);
        assertThat(allFX.entrySet()).hasSize(3);
    }

    @Test
    public void testPersistBond()
    {
        List<String> lines = new ArrayList<>();
        lines.add("DE114173=RRPS,.01,EUR,DE0001141737,\"GERMANY, FEDERAL REPUBLIC OF (GOVERNMENT)\"");
        lines.add("JP03560042=RRPS,\"50,000\",JPY,JP1023561F93,JAPAN (GOVERNMENT OF)");
        dataScopePersistService.persistBond(lines);

        Asset asset = assetService.find("JP1023561F93");
        Assert.assertEquals(asset.getParValue().doubleValue(), 50000, 0.1);
    }
}
