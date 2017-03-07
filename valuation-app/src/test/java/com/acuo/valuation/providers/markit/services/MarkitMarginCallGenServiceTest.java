package com.acuo.valuation.providers.markit.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.MarginCallGenService;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
public class MarkitMarginCallGenServiceTest  {

    @Inject
    MarginCallGenService marginCallGenService;



    @Test
    public void testGenMarginCall()
    {
//        PortfolioId portfolio = PortfolioId.fromString("p18");
//        Set<PortfolioId> portfolios = new HashSet<PortfolioId>();
//        portfolios.add(portfolio);
//        marginCallGenService.marginCalls(portfolios, LocalDate.now());
    }


}
