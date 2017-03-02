package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.entity.Value;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.opengamma.strata.collect.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
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
public class MarginResultPersisterTest {

    @Inject
    ImportService importService;

    @Inject
    ValuationService valuationService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ValueService valueService;

    MarginResultPersister persister;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        persister = new MarginResultPersister(valuationService, portfolioService, valueService);
    }

    @Test
    public void testPersistClarusResult() {
        MarginValuation marginValuation = new MarginValuation("USD", 1d, 1d, 1d, null);
        Result<MarginValuation> result = Result.success(marginValuation);
        MarginResults marginResults = new MarginResults();
        marginResults.setResults(Arrays.asList(result));
        marginResults.setPortfolioId("p2");
        LocalDate localDate = LocalDate.now();
        marginResults.setValuationDate(localDate);
        marginResults.setCurrency("USD");
        persister.persist(marginResults);
        Portfolio portfolio = portfolioService.findById("p2");
        Set<Valuation> valuationSet = portfolio.getValuations();
        for (Valuation valuation : valuationSet) {
            Assert.assertEquals(localDate, valuation.getDate());
            Set<Value> values = valuation.getValues();
            for (Value value : values) {
                Assert.assertEquals(value.getPv().doubleValue(), 1d, 0);
            }
        }
    }
}
