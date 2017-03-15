package com.acuo.valuation.providers.markit.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
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
