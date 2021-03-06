package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.common.model.ids.TradeId;
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
import com.acuo.valuation.protocol.responses.Response;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.services.TradeUploadService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
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
public class MarkitResultPersisterTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private ValuationService valuationService = null;

    @Inject
    private ValueService valueService = null;

    @Inject
    private ResponseParser parser = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile large = new ResourceFile("/markit/responses/large.xml");

    private MarkitResultPersister persister;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());
        persister = new MarkitResultPersister(valuationService, valueService);
    }

    @Test
    public void testPersistValidPricingResult() throws ParseException {
        LocalDate localDate = LocalDate.now();
        String tradeId = "455123";

        MarkitResults markitResults = markitResults(localDate, tradeId);

        persister.persist(markitResults);

        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));
        assertThat(valuation).isNotNull();
        Set<TradeValue> values = valuation.getValues();
        assertThat(values).isNotNull().hasSize(1);

        for (TradeValue value : values) {
            if(value.getValuationDate().equals(localDate)){
                assertThat(value.getPv().doubleValue()).isEqualTo(-30017690);
            }
        }
    }

    @Test
    public void testPersistValidPricingResultTwice() throws ParseException {
        LocalDate localDate = LocalDate.now();
        String tradeId = "455123";

        MarkitResults markitResults = markitResults(localDate, tradeId);

        persister.persist(markitResults);
        persister.persist(markitResults);

        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));
        assertThat(valuation).isNotNull();
        Set<TradeValue> values = valuation.getValues();
        assertThat(values).isNotNull().hasSize(1);

        for (TradeValue value : values) {
            if(value.getValuationDate().equals(localDate)){
                assertThat(value.getPv().doubleValue()).isEqualTo(-30017690);
            }
        }
    }

    private MarkitResults markitResults(LocalDate myDate1, String tradeId) {
        List<Result<MarkitValuation>> results = new ArrayList<>();
        MarkitValue markitValue = new MarkitValue();
        markitValue.setTradeId(tradeId);
        markitValue.setPv((double) -30017690);
        MarkitValuation markitValuation = new MarkitValuation(markitValue);
        Result<MarkitValuation> result = Result.success(markitValuation);
        results.add(result);
        MarkitResults markitResults = new MarkitResults();
        markitResults.setResults(results);
        markitResults.setValuationDate(myDate1);
        markitResults.setCurrency(Currency.USD);
        return markitResults;
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

        MarkitResults markitResults = new MarkitResults();
        markitResults.setResults(results);
        markitResults.setValuationDate(response.header().getDate());
        markitResults.setCurrency(Currency.parse(response.header().getValuationCurrency()));

        persister.persist(markitResults);

        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString("455820"));
        Set<TradeValue> values = valuation.getValues();
        assertThat(values).hasSize(1);
    }
}
