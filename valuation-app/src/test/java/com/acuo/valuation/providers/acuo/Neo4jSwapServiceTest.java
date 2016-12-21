package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.DataImporter;
import com.acuo.persist.core.DataLoader;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.*;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.*;
import com.acuo.valuation.modules.*;

import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.assertj.core.api.Condition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
                                MappingModule.class,
                                EncryptionModule.class,
                                Neo4jPersistModule.class,
                                DataLoaderModule.class,
                                DataImporterModule.class,
                                RepositoryModule.class,
                                EndPointModule.class,
                                ServicesModule.class})
public class Neo4jSwapServiceTest {

    @Mock
    PricingService pricingService;

    @Inject
    Neo4jPersistService session;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    TradeService<Trade> tradeService;

    @Inject
    DataLoader dataLoader;

    @Inject
    DataImporter dataImporter;

    @com.google.inject.Inject
    ValuationService valuationService;

    @com.google.inject.Inject
    PortfolioService portfolioService;

    @com.google.inject.Inject
    ValueService valueService;

    @com.google.inject.Inject
    AccountService accountService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    Neo4jSwapService service;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        dataLoader.purgeDatabase();
        dataLoader.createConstraints();
        dataImporter.importFiles("clients", "legalentities", "accounts");
        service = new Neo4jSwapService(pricingService, /*session,*/ tradeService, valuationService, portfolioService, valueService);
        tradeUploadService.uploadTradesFromExcel(oneIRS.getInputStream());

        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioId("p2");
        portfolio = portfolioService.createOrUpdate(portfolio);
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(pricingService.price(any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.price("455123");

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testPriceSwapFromClientId() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(pricingService.price(any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.priceClientTrades("c1");

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);

    }

    @Test
    public void testPersistValidPricingResult() throws ParseException {
        List<Result<MarkitValuation>> results = new ArrayList<Result<MarkitValuation>>();

        MarkitValue markitValue = new MarkitValue();

        markitValue.setTradeId("455123");
        markitValue.setPv(5.98);

        MarkitValuation markitValuation = new MarkitValuation(markitValue);

        Result<MarkitValuation> result = Result.success(markitValuation);

        results.add(result);

        PricingResults pricingResults = PricingResults.of(results);

        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate myDate1 = LocalDate.of(2015, 6, 1);
        pricingResults.setDate(myDate1);
        pricingResults.setCurrency(Currency.USD);
        service.persistMarkitResult(pricingResults);

        Trade trade = tradeService.findById(455123l);
        Set<Valuation> valuations  = trade.getValuations();
        boolean foundValuation = false;
        boolean foundValue = false;
        for(Valuation valuation : valuations)
        {
            if(valuation.getDate().equals(myDate1))
            {
                foundValuation = true;
                Set<Value> values = valuation.getValues();
                for(Value value : values)
                {
                    if(value.getCurrency().equals(Currency.USD) && value.getSource().equals("Markit") && value.getPv().doubleValue() ==5.98)
                    {
                        foundValue = true;
                    }

                }
            }

        }

        Assert.assertTrue(foundValuation);
        Assert.assertTrue(foundValue);
    }

    @Test
    public void testPersistNullPricingResult() throws ParseException {
        service.persistMarkitResult(null);
    }

    @Test
    public void testPersistClarusResult()
    {
        MarginValuation marginValuation = new MarginValuation("USD", 1d, 1d, 1d);
        Result<MarginValuation> result = Result.success(marginValuation);
        MarginResults marginResults = MarginResults.of(Arrays.asList(result));
        marginResults.setPortfolioId("p2");
        LocalDate localDate = LocalDate.now();
        marginResults.setValuationDate(localDate);
        marginResults.setCurrency("USD");
        Assert.assertTrue(service.persistClarusResult(marginResults));
        Portfolio portfolio = portfolioService.findById("p2");
        Set<Valuation> valuationSet = portfolio.getValuations();
        for(Valuation valuation : valuationSet)
        {
            Assert.assertEquals(localDate, valuation.getDate());
            Set<Value> values = valuation.getValues();
            for(Value value : values)
            {
                Assert.assertEquals(value.getPv().doubleValue(), 1d,0);
            }
        }
    }
}
