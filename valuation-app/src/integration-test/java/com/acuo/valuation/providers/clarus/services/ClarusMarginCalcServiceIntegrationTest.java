package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationIntegrationTestModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import com.acuo.valuation.services.MarginCalcService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.utils.SwapTradeBuilder;
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
public class ClarusMarginCalcServiceIntegrationTest {

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Inject
    MarginCalcService service;

    @Inject
    ImportService importService;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    TradeService<Trade> irsService;

    @Before
    public void setup() {
        importService.reload();
        tradeUploadService.uploadTradesFromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testWithMapper() throws IOException {
        String id = "455123";
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        Trade trade = irsService.find(TradeId.fromString(id));
        if (trade != null) {
            SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS) trade);
            swapTrades.add(swapTrade);
        }
        MarginResults response = service.send(swapTrades, DataModel.LCH, MarginCallType.VM);
        assertThat(response).isNotNull();
    }
}