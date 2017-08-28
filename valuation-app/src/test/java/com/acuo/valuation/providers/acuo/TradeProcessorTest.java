package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.util.AbstractMockServerTest;
import com.acuo.valuation.util.MockQueueServerModule;
import com.acuo.valuation.util.MockStaticServerModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MockStaticServerModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class TradeProcessorTest extends AbstractMockServerTest {

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeService<Trade> tradeService = null;

    @Inject
    private TradeProcessor processor = null;

    private List<Trade> trades;

    @Before
    public void setUp() throws Exception {
        importService.reload();
        final List<String> tradeIds = tradeUploadService.fromExcel(oneIRS.createInputStream());

        trades = tradeIds.stream()
                .map(id -> tradeService.find(TradeId.fromString(id)))
                .collect(toList());
    }

    @Test
    public void testSingleCallProcess() throws Exception {

        List<MarginCall> marginCalls = processor.process(trades);

        assertThat(marginCalls).isNotEmpty().hasSize(3);
        final MarginCall marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);
    }

    @Test
    public void testDoubleCallProcess() throws Exception {

        List<MarginCall> marginCalls = processor.process(trades);
        assertThat(marginCalls).isNotEmpty().hasSize(3);
        MarginCall marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);

        marginCalls = processor.process(trades);

        assertThat(marginCalls).isNotEmpty().hasSize(3);
        marginCall = marginCalls.get(0);
        assertThat(marginCall).isNotNull();
        assertThat(marginCall.getMarginStatement()).isNotNull();
        assertThat(marginCall.getLastStep().getStatus()).isEqualTo(StatementStatus.MatchedToReceived);
    }
}