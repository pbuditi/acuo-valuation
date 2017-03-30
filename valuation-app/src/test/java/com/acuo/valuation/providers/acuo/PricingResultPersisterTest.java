package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.entity.Value;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.results.PricingResultPersister;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
public class PricingResultPersisterTest {

    @Inject
    ImportService importService;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    TradeService<Trade> tradeService;

    @Inject
    ValuationService valuationService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ValueService valueService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    PricingResultPersister persister;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.uploadTradesFromExcel(oneIRS.createInputStream());
        persister = new PricingResultPersister(tradeService, valuationService, valueService);
    }
    @Test
    public void testPersistValidPricingResult() throws ParseException {
        List<Result<MarkitValuation>> results = new ArrayList<Result<MarkitValuation>>();

        String tradeId = "455123";

        MarkitValue markitValue = new MarkitValue();

        markitValue.setTradeId(tradeId);
        markitValue.setPv(new Double(-30017690));

        MarkitValuation markitValuation = new MarkitValuation(markitValue);

        Result<MarkitValuation> result = Result.success(markitValuation);

        results.add(result);

        PricingResults pricingResults = new PricingResults();
        pricingResults.setResults(results);

        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate myDate1 = LocalDate.now();
        pricingResults.setDate(myDate1);
        pricingResults.setCurrency(Currency.USD);
        persister.persist(pricingResults);

        Trade trade = tradeService.findById(tradeId);
        Set<Valuation> valuations = trade.getValuations();
        boolean foundValuation = false;
        boolean foundValue = false;
        for (Valuation valuation : valuations) {
            if (valuation.getDate().equals(myDate1)) {
                foundValuation = true;
                Set<Value> values = valuation.getValues();
                if (values != null) {
                    for (Value value : values) {
                        if (value.getCurrency().equals(Currency.USD) && value.getSource().equals("Markit") && value.getPv().doubleValue() == 5.98) {
                            foundValue = true;
                        }
                    }
                }
            }

        }

        Assert.assertTrue(foundValuation);
    }

    @Test
    public void testPersistNullPricingResult() throws ParseException {
        persister.persist(null);
    }
}
