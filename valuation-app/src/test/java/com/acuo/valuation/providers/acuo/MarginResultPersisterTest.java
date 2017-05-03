package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
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
import com.acuo.valuation.providers.acuo.results.MarginResultPersister;
import com.opengamma.strata.collect.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
        persister = new MarginResultPersister(valuationService, valueService);
    }

    @Test
    public void testPersistClarusResult() {
        LocalDate localDate = LocalDate.now();
        MarginResults marginResults = marginResults(localDate);
        persister.persist(marginResults);
        com.acuo.persist.entity.MarginValuation valuation = valuationService.getMarginValuationFor(PortfolioId.fromString("p2"));
        assertThat(valuation).isNotNull();
        Set<MarginValueRelation> values = valuation.getValues();
        assertThat(values).isNotNull().hasSize(1);
        for (MarginValueRelation value : values) {
            if(value.getDateTime().equals(localDate)){
                MarginValue tradeValue = value.getValue();
                assertThat(tradeValue.getAmount().doubleValue()).isEqualTo(1d);
            }
        }
    }

    @Test
    public void testPersistClarusResultTwice() {
        LocalDate localDate = LocalDate.now();
        MarginResults marginResults = marginResults(localDate);
        persister.persist(marginResults);
        persister.persist(marginResults);
        com.acuo.persist.entity.MarginValuation valuation = valuationService.getMarginValuationFor(PortfolioId.fromString("p2"));
        assertThat(valuation).isNotNull();
        Set<MarginValueRelation> values = valuation.getValues();
        assertThat(values).isNotNull().hasSize(1);
        for (MarginValueRelation value : values) {
            if(value.getDateTime().equals(localDate)){
                MarginValue tradeValue = value.getValue();
                assertThat(tradeValue.getAmount().doubleValue()).isEqualTo(1d);
            }
        }
    }

    private MarginResults marginResults(LocalDate localDate) {
        MarginValuation marginValuation = new MarginValuation("USD", 1d, 1d, 1d, null, "p2");
        Result<MarginValuation> result = Result.success(marginValuation);
        MarginResults marginResults = new MarginResults();
        marginResults.setResults(Collections.singletonList(result));
        marginResults.setValuationDate(localDate);
        marginResults.setCurrency("USD");
        return marginResults;
    }
}
