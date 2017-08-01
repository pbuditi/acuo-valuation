package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.ids.ClientId;
import com.acuo.common.model.ids.TradeId;
import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.builders.TradeConverter;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.TradeUploadService;
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
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
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
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class MarkitPricingServiceTest {

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile all = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Rule
    public ResourceFile test02 = new ResourceFile("/markit/reports/markit-test-02.xml");

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Inject
    private ReportParser reportParser = null;

    @Inject
    private ValuationService valuationService = null;

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeService<Trade> tradeService = null;

    @Mock
    private Sender sender;

    @Mock
    private Retriever retriever;

    private MarkitPricingService service;

    private List<com.acuo.common.model.trade.Trade> trades;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        service = new MarkitPricingService(sender, retriever, tradeService, valuationService);

        importService.reload();

        trades = loadTrades(oneIRS);
    }

    @Test
    public void testPriceSwapWithErrorReport() {
        when(sender.send(any(), any(LocalDate.class))).thenReturn(ReportHelper.reportError());

        MarkitResults markitResults = new MarkitResults();
        markitResults.setResults(Collections.emptyList());
        when(retriever.retrieve(any(LocalDate.class), any())).thenReturn(markitResults);

        MarkitResults results = service.priceSwapTrades(trades);

        assertThat(results).isNotNull();
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(), any(LocalDate.class))).thenReturn(ReportHelper.report());
        when(retriever.retrieve(any(LocalDate.class), any())).thenReturn(expectedResults());

        MarkitResults results = service.priceSwapTrades(trades);

        assertThat(results).isNotNull().isInstanceOf(MarkitResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<>(s -> s.getValue().getValue().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testPriceSwapWithReportFromFile() throws Exception {
        when(sender.send(any(), any(LocalDate.class))).thenReturn(reportParser.parse(test02.getContent()));
        when(retriever.retrieve(any(LocalDate.class), any())).thenReturn(expectedResults());

        MarkitResults results = service.priceSwapTrades(Collections.singletonList(SwapHelper.createTrade()));

        assertThat(results).isNotNull().isInstanceOf(MarkitResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<>(s -> s.getValue().getValue().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    @Test
    public void testPriceSwapFromClientId() throws Exception {

        when(sender.send(any(), any(LocalDate.class))).thenReturn(reportParser.parse(test02.getContent()));
        when(retriever.retrieve(any(LocalDate.class), any())).thenReturn(expectedResults());

        MarkitResults results = service.priceTradesOf(ClientId.fromString("c1"));

        assertThat(results).isNotNull().isInstanceOf(MarkitResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<>(s -> s.getValue().getValue().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }

    private MarkitResults expectedResults() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setTradeId("id1");
        markitValue.setPv(1.0d);

        MarkitResults expectedResults = new MarkitResults();
        expectedResults.setResults(Collections.singletonList(Result.success(new MarkitValuation(markitValue))));
        return expectedResults;
    }

    @Test
    public void testPriceTradesByBulk() throws Exception {

        when(sender.send(any(), any(LocalDate.class))).thenReturn(reportParser.parse(largeReport.getContent()));
        when(retriever.retrieve(any(LocalDate.class), any())).thenReturn(expectedResults());

        List<com.acuo.common.model.trade.Trade> all = loadTrades(this.all);

        MarkitResults results = service.priceSwapTradesByBulk(all);

        assertThat(results).isNotNull().isInstanceOf(MarkitResults.class);

    }

    private List<com.acuo.common.model.trade.Trade> loadTrades(ResourceFile file) {
        final List<String> tradeIds = tradeUploadService.fromExcel(file.createInputStream());

        return tradeIds.stream()
                .map(id -> tradeService.find(TradeId.fromString(id)))
                .map(TradeConverter::buildTrade)
                .collect(toList());
    }
}