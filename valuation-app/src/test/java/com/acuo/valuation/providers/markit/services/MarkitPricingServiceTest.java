package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.util.ReportHelper;
import com.google.inject.Inject;
import com.opengamma.strata.collect.result.Result;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Named;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, EncryptionModule.class, Neo4jPersistModule.class, EndPointModule.class, ServicesModule.class})

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

    @javax.inject.Inject
    Neo4jPersistService session;

    @Inject
    ReportParser reportParser;

    @Mock
    Sender sender;

    @Mock
    Retriever retriever;

    MarkitPricingService service;

    List<SwapTrade> swaps;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        service = new MarkitPricingService(sender, retriever, session);

        swaps = clarusTransformer.deserialiseToList(cmeCsv.getContent());

    }

    @Test
    public void testPriceSwapWithErrorReport() {
        when(sender.send(any(List.class))).thenReturn(ReportHelper.reportError());

        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(PricingResults.of(Collections.EMPTY_LIST));

        PricingResults results = service.price(swaps);

        assertThat(results).isNotNull();
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(List.class))).thenReturn(ReportHelper.report());
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.price(swaps);

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
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.price(asList(SwapHelper.createTrade()));

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testSavePv() throws ParseException
    {
        List<Result<MarkitValuation>> results = new ArrayList<Result<MarkitValuation>>();

        MarkitValue markitValue = new MarkitValue();

        markitValue.setTradeId("ndft2");
        markitValue.setPv(5.98);

        MarkitValuation markitValuation = new MarkitValuation(markitValue);

        Result<MarkitValuation> result = Result.success(markitValuation);

        results.add(result);

        PricingResults pricingResults = PricingResults.of(results);

        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate1 = dateFormat1.parse("2015-06-01");
        pricingResults.setDate(myDate1);
        pricingResults.setCurrency("USD");
        service.savePv(pricingResults);
    }
}