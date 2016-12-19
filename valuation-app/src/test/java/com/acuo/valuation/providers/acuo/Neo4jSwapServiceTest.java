package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.DataImporter;
import com.acuo.persist.core.DataLoader;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.Account;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AccountService;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.*;

import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
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
@Slf4j
public class Neo4jSwapServiceTest {

    @Mock
    PricingService pricingService;

    @Inject
    Neo4jPersistService session;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    TradeService tradeService;

    @Inject
    DataLoader dataLoader;

    @Inject
    DataImporter dataImporter;

    @Inject
    AccountService accountService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    Neo4jSwapService service;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        dataLoader.purgeDatabase();
        dataImporter.importFiles("clients", "legalentities", "accounts");
        service = new Neo4jSwapService(pricingService, /*session,*/ tradeService);
        tradeUploadService.uploadTradesFromExcel(oneIRS.getInputStream());
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
        service.persist(pricingResults);
    }

    @Test
    public void testPersistNullPricingResult() throws ParseException {
        service.persist(null);
    }
}
