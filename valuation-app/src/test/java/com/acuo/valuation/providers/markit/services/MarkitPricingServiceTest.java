package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.util.ReportHelper;
import com.opengamma.strata.collect.result.Result;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})

public class MarkitPricingServiceTest {

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile test02 = new ResourceFile("/markit/reports/markit-test-02.xml");

    @Inject
    @Named("clarus")
    Transformer<SwapTrade> clarusTransformer;

    @Inject
    @Named("markit")
    Transformer<SwapTrade> markitTransformer;

    @Inject
    ReportParser reportParser;

    @Inject
    TradeService<Trade> tradeService;

    @Mock
    Sender sender;

    @Mock
    Retriever retriever;

    MarkitPricingService service;

    List<SwapTrade> swaps;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        service = new MarkitPricingService(sender, retriever, tradeService);

        swaps = clarusTransformer.deserialiseToList(cmeCsv.getContent());
    }

    @Test
    public void testPriceSwapWithErrorReport() {
        when(sender.send(any(List.class))).thenReturn(ReportHelper.reportError());

        PricingResults pricingResults = new PricingResults();
        pricingResults.setResults(Collections.EMPTY_LIST);
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(pricingResults);

        PricingResults results = service.priceSwapTrades(swaps);

        assertThat(results).isNotNull();
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(List.class))).thenReturn(ReportHelper.report());
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = new PricingResults();
        expectedResults.setResults(asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.priceSwapTrades(swaps);

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testPriceSwapWithReportFromFile() throws Exception {
        when(sender.send(any(List.class))).thenReturn(reportParser.parse(test02.getContent()));
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = new PricingResults();
        expectedResults.setResults(asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.priceSwapTrades(asList(SwapHelper.createTrade()));

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testPriceSwapFromClientId() throws Exception {

        when(sender.send(any(List.class))).thenReturn(reportParser.parse(test02.getContent()));

        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);

        PricingResults expectedResults = new PricingResults();
        expectedResults.setResults(asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.priceTradesOf(ClientId.fromString("c1"));

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }
}