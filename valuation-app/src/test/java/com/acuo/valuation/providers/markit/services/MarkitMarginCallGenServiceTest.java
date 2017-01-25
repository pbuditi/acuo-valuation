package com.acuo.valuation.providers.markit.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.MarginCallGenService;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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
public class MarkitMarginCallGenServiceTest {

    @Inject
    MarginCallGenService marginCallGenService;

    @Inject
    AgreementService agreementService;

    @Inject
    ValuationService valuationService;

    @Inject
    PortfolioService portfolioService;

    @Test
    public void testGeneareteMarginCall()
    {
        Agreement agreement = agreementService.findById("a12");
        Portfolio portfolio = portfolioService.findById("p12");
        Valuation valuation = valuationService.findById("2017/01/25-455773");
        marginCallGenService.geneareteMarginCall(agreement,portfolio,valuation);
    }
}
