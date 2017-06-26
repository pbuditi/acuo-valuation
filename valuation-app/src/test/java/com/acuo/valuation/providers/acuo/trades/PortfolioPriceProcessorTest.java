package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.modules.*;
import com.acuo.valuation.jackson.PortfolioIds;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
@Slf4j
public class PortfolioPriceProcessorTest {

    @Inject
    PortfolioPriceProcessor portfolioPriceProcessor;

    @Inject
    ImportService importService;

    @Before
    public void setup() throws IOException {
        //importService.reload();
    }

    @Test
    public void process() throws Exception {

        PortfolioIds portfolioIds = new PortfolioIds();
        List<String> ids = new ArrayList<>();
        ids.add("11");
        ids.add("22");
        portfolioIds.setIds(ids);
        ObjectMapper mapper = new ObjectMapper();
        log.info(mapper.writeValueAsString(portfolioIds));
    }
}
