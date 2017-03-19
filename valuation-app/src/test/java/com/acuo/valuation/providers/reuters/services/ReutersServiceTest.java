package com.acuo.valuation.providers.reuters.services;

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
        String json = "{\n" +
                "\"AsOfDate\":\"2017-03-10\",\n" +
                " \"Sections\" : {\n" +
                "    \"Description\" :\"true\",\n" +
                "    \"Quote\":\"true\",\n" +
                "    \"PricingAnalysis\" :\"true\",\n" +
                "    \"Valuation\":\"true\",\n" +
                "    \"RiskMeasures\":\"true\"\n" +
                "  },\n" +
                "\t\"MtMItems\": {\n" +
                "\t\t\"SecurityInputs\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"Guid\": \"Id1\",\n" +
                "\t\t\t\"SecurityId\": \"US38143U8G98\",\n" +
                "\t\t\t\"Notional\": 2000000,\n" +
                "\t\t\t\"PriceType\":\"Bid\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"Guid\": \"Id2\",\n" +
                "\t\t\t\"SecurityId\":\"PL106068=\",\n" +
                "\t\t\t\"Notional\": 2000000,\n" +
                "\t\t\t\"price\":103.45360000\n" +
                "\t\t},\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}";
        String resposne = reutersService.send(json);
        log.info(resposne);
    }

}
