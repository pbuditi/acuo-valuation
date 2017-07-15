package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.results.TradeValuation;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.trades.PortfolioValuationPersister;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

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
public class PortfolioValuationPersisterTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private ValuationService valuationService = null;

    @Inject
    private ValueService valueService = null;

    private PortfolioValuationPersister persister = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());
        persister = new PortfolioValuationPersister(valuationService, valueService);
    }

    @Test
    public void testPersistValidPricingResult() throws ParseException {
        LocalDate localDate = LocalDate.now();
        String tradeId = "455123";

        PortfolioResults markitResults = portfolioResults(localDate, tradeId);

        persister.persist(markitResults);

        com.acuo.persist.entity.TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));
        assertThat(valuation).isNotNull();
        Set<TradeValue> values = valuation.getValues();
        assertThat(values).isNotNull().hasSize(1);

        for (TradeValue value : values) {
            if(value.getValuationDate().equals(localDate)){
                assertThat(value.getPv().doubleValue()).isEqualTo(-30017690);
            }
        }
    }

    private PortfolioResults portfolioResults(LocalDate myDate1, String tradeId) {
        List<Result<com.acuo.common.model.results.TradeValuation>> results = new ArrayList<>();
        com.acuo.common.model.results.TradeValuation markitValue = new com.acuo.common.model.results.TradeValuation();
        markitValue.setTradeId(tradeId);
        markitValue.setMarketValue(-30017690.0d);
        markitValue.setValuationDate(LocalDateUtils.minus(LocalDate.now(), 1));
        Result<TradeValuation> result = Result.success(markitValue);
        results.add(result);
        PortfolioResults markitResults = new PortfolioResults();
        markitResults.setResults(results);
        markitResults.setValuationDate(myDate1);
        markitResults.setCurrency(Currency.USD);
        return markitResults;
    }
}
