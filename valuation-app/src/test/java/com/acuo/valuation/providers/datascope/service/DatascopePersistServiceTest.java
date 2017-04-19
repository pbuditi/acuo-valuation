package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.CurrencyEntity;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.CurrencyService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class DatascopePersistServiceTest {

    @Inject
    DatascopePersistService datascopePersistService;

    @Inject
    ImportService importService;

    @Inject
    CurrencyService currencyService;

    @Before
    public void setup() throws IOException {
        importService.reload();
    }

    @Test
    public void testPersistFxRate()
    {
        List<String> lines = new ArrayList<>();
        lines.add("BHDUSD=R,2.65252,04/17/2017 20:52:03");
        lines.add("BMDUSD=R,,04/17/2017 21:02:00");
        datascopePersistService.persistFxRate(lines);

        CurrencyEntity currencyEntity = currencyService.find("BHD");
        log.info("fx:" + currencyEntity.getFxRateRelation().getFxRate());

    }
}
