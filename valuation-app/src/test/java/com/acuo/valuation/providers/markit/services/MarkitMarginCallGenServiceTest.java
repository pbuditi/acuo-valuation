package com.acuo.valuation.providers.markit.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.*;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.*;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.Neo4jSwapService;
import com.acuo.valuation.providers.acuo.Neo4jSwapServiceTest;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.MarginCallGenService;
import com.acuo.valuation.services.PricingService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
    MarginStatementService marginStatementService;

    @Test
    public void testGenMarginCall()
    {
//        testPriceSwapWithNoErrorReport();
//        String msId = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "-a31";
//        MarginStatement marginStatement = marginStatementService.findById(msId);
//        Assert.assertNotNull(marginStatement);
//        for(MarginCall marginCall : marginStatement.getMarginCalls())
//        {
//            Assert.assertNotNull(marginCall.getExcessAmount());
//        }
    }


}
