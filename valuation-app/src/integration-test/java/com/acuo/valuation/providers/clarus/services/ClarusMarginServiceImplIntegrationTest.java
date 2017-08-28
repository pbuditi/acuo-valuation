package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.builders.TradeConverter;
import com.acuo.valuation.modules.ConfigurationIntegrationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.services.TradeUploadService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationIntegrationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class ClarusMarginServiceImplIntegrationTest {

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Inject
    private ClarusMarginService service = null;

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeService<Trade> irsService = null;

    @Before
    public void setup() {
        importService.reload();
        tradeUploadService.fromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testWithMapper() throws IOException {
        String id = "455123";
        List<com.acuo.common.model.trade.Trade> trades = new ArrayList<>();
        Trade entity = irsService.find(TradeId.fromString(id));
        if (entity != null) {
            com.acuo.common.model.trade.Trade trade = TradeConverter.buildTrade(entity);
            trades.add(trade);
        }
        MarginResults response = service.send(trades, DataModel.LCH, MarginCallType.VM);
        assertThat(response).isNotNull();
    }
}