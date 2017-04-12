package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.*;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.responses.Response;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.results.PricingResultPersister;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
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

    @Inject
    ResponseParser parser;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile large = new ResourceFile("/markit/responses/large.xml");

    PricingResultPersister persister;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.uploadTradesFromExcel(oneIRS.createInputStream());
        persister = new PricingResultPersister(tradeService, valuationService, valueService, portfolioService);
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

        LocalDate myDate1 = LocalDate.now();
        pricingResults.setDate(myDate1);
        pricingResults.setCurrency(Currency.USD);
        persister.persist(pricingResults);

        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));
        boolean foundValue = false;

        Set<TradeValueRelation> values = valuation.getValues();
        for (TradeValueRelation value : values) {
            TradeValue tradeValue = value.getValue();
            if (value.getDateTime().equals(myDate1) && tradeValue.getCurrency().equals(Currency.USD) && tradeValue.getSource().equals("Markit") && tradeValue.getPv().equals(new Double(-30017690)))
                foundValue = true;
        }
        Assert.assertTrue(foundValue);
    }

    @Test
    public void testPersistNullPricingResult() throws ParseException {
        persister.persist(null);
    }

    @Test
    public void testWithLargeResponse() throws Exception {
        Response response = parser.parse(large.getContent());

        List<String> tradeIds = ImmutableList.of("455820");

        List<Result<MarkitValuation>> results = tradeIds.stream()
                .map(tradeId -> response.values()
                        .stream()
                        .filter(value -> tradeId.equals(value.getTradeId()))
                        .filter(value -> !"Failed".equalsIgnoreCase(value.getStatus()))
                        .collect(toList()))
                .map(MarkitValuation::new)
                .map(Result::success)
                .collect(toList());

        PricingResults pricingResults = new PricingResults();
        pricingResults.setResults(results);
        pricingResults.setDate(response.header().getDate());
        pricingResults.setCurrency(Currency.parse(response.header().getValuationCurrency()));

        persister.persist(pricingResults);

        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString("455820"));
        Set<TradeValueRelation> values = valuation.getValues();
        assertThat(values).hasSize(1);
    }
}
