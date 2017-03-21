package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.ReutersService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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

    @Inject
    private ReutersService reutersService;

    @Test
    @Ignore
    public void testSend()
    {
        Assets assets = new Assets();
        assets.setAssetId("1231");
        assets.setAvailableQuantities(11);
        assets.setCurrency(java.util.Currency.getInstance("USD"));
        assets.setFitchRating("1");
        String resposne = reutersService.send(assets);
        log.info(resposne);
    }

}
