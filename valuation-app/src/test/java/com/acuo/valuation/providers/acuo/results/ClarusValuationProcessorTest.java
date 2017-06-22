package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.model.margin.Types;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
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
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import org.junit.Before;
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
public class ClarusValuationProcessorTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private ClarusValuationProcessor processor = null;

    @Mock
    private MarginResults results;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        importService.reload();
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
        MarginValuation valuation = new MarginValuation("test",
                10.0d,
                10.0d,
                10.0d,
                Types.CallType.Variation,
                "p31");
        when(results.getResults()).thenReturn(ImmutableList.of(Result.success(valuation)));
        when(results.getMarginType()).thenReturn(Types.CallType.Variation);
        when(results.getValuationDate()).thenReturn(LocalDate.now());
        when(results.getCurrency()).thenReturn(Currency.USD.getCode());
    }

}