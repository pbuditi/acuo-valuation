package com.acuo.valuation.providers.acuo;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.acuo.trades.TradeUploadServicePoi;
import com.acuo.valuation.providers.acuo.trades.TradeUploadServiceTransformer;
import com.acuo.valuation.services.TradeUploadService;
import com.googlecode.junittoolbox.MultithreadingTester;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
@Slf4j
public class TradeUploadServiceTransformerTest {

    private TradeUploadService service;

    @Inject
    private TradingAccountService accountService = null;

    @Inject
    private PortfolioService portfolioService = null;

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeService<Trade> tradeService = null;

    @Inject
    @Named("portfolio")
    private Transformer<SwapTrade> transformer = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio18.xlsx");

    @Rule
    public ResourceFile qa = new ResourceFile("/excel/TradePortfolioQA.xlsx");

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new TradeUploadServiceTransformer(accountService, portfolioService, tradeService, transformer);
        importService.reload();
    }

    @Test
    public void testUploadOneIRS() {
        List<String> tradeIds = service.fromExcel(oneIRS.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull().hasSize(2);
    }

    @Test
    public void testUploadAll() throws IOException {
        List<String> tradeIds = service.fromExcel(excel.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull().hasSize(799);
    }

    @Test
    public void testHandleIRSOneRowUpdate() throws IOException {
        service.fromExcel(oneIRS.createInputStream());
        service.fromExcel(oneIRS.createInputStream());
        Iterable<Trade> irses = tradeService.findAll();
        assertThat(irses).isNotEmpty().hasSize(2);
    }

    @Test
    public void testConcurrentUpload() {
        new MultithreadingTester().numThreads(10).numRoundsPerThread(1).add(() -> {
            service.fromExcel(oneIRS.createInputStream());
            Thread.sleep(1000);
            return null;
        }).run();
    }

    @Test
    public void testCompareVersion() {
        final TradeUploadServicePoi oldService = new TradeUploadServicePoi(accountService, portfolioService, tradeService);
        oldService.fromExcel(qa.createInputStream());
        Iterator<Trade> olds = tradeService.findAll(2).iterator();
        importService.reload();
        service.fromExcel(excel.createInputStream());
        while (olds.hasNext()) {
            Trade versionOld = olds.next();
            Trade versionNew = tradeService.find(versionOld.getTradeId(), 2);

            if (!(versionNew instanceof FRA) && !versionOld.equals(versionNew)) {
                log.info("old:" + versionOld.toString());
                log.info("new:" + versionNew.toString());
                break;
            } else {
                log.info("trade match:" + versionOld.getTradeId());
            }
        }

    }

    @Test
    public void testUploadAllNew() throws IOException {
        List<String> tradeIds = service.fromExcel(excel.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull();
    }
}