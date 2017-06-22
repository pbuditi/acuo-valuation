package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.services.TradeUploadService;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.collect.result.ValueWithFailures;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class MarkitValuationProcessorTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private MarkitValuationProcessor processor = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Mock
    private MarkitResults results;

    @Mock
    private MarkitValuation valuation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testSingleCallProcess() throws Exception {
        mockConditions();

        List<MarginCall> marginCalls = processor.process(results);

        assertThat(marginCalls).isNotEmpty().hasSize(1);
        final MarginCall marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);
    }

    @Test
    public void testDoubleCallProcess() throws Exception {
        mockConditions();

        List<MarginCall> marginCalls = processor.process(results);
        assertThat(marginCalls).isNotEmpty().hasSize(1);
        MarginCall marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);

        marginCalls = processor.process(results);

        assertThat(marginCalls).isNotEmpty().hasSize(1);
        marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);
    }

    private void mockConditions() {
        when(results.getResults()).thenReturn(ImmutableList.of(Result.success(valuation)));
        when(results.getValuationDate()).thenReturn(LocalDate.now());
        when(results.getCurrency()).thenReturn(Currency.USD);
        when(valuation.getTradeId()).thenReturn("455820");
        when(valuation.getValue()).thenReturn(ValueWithFailures.of(10.0d));
    }

}