package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.margin.Types;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.common.model.ids.PortfolioId;
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
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class PortfolioProcessorTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private ResultPersister<MarkitResults> markitPersister = null;

    @Inject
    private ResultPersister<MarginResults> marginPersister = null;

    @Inject
    private PortfolioProcessor processor = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Mock
    private MarkitResults markitResults;

    @Mock
    private MarginResults marginResults;

    private Set<PortfolioId> portfolioIds = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());

        mockConditions();

        portfolioIds.addAll(markitPersister.persist(markitResults));
        portfolioIds.addAll(marginPersister.persist(marginResults));
    }

    @Test
    public void process() throws Exception {

        List<MarginCall> marginCalls = processor.process(portfolioIds);

        assertThat(marginCalls).isNotEmpty().hasSize(2);
        final MarginCall marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);

    }

    private void mockConditions() {
        MarginValuation marginValuation = new MarginValuation("test",
                10.0d,
                10.0d,
                10.0d,
                Types.CallType.Variation,
                "p31");
        when(marginResults.getResults()).thenReturn(ImmutableList.of(Result.success(marginValuation)));
        when(marginResults.getMarginType()).thenReturn(Types.CallType.Variation);
        when(marginResults.getValuationDate()).thenReturn(LocalDate.now());
        when(marginResults.getCurrency()).thenReturn(Currency.USD.getCode());

        MarkitValuation markitValuation = new MarkitValuation("455820", ValueWithFailures.of(10.0d));
        when(markitResults.getResults()).thenReturn(ImmutableList.of(Result.success(markitValuation)));
        when(markitResults.getValuationDate()).thenReturn(LocalDate.now());
        when(markitResults.getCurrency()).thenReturn(Currency.USD);
    }
}