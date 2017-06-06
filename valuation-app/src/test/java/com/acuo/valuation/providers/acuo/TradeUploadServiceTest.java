package com.acuo.valuation.providers.acuo;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.TradeId;
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
import com.acuo.valuation.providers.acuo.trades.TradeUploadServiceImpl;
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
public class TradeUploadServiceTest {

    TradeUploadServiceImpl service;

    @Inject
    TradingAccountService accountService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ImportService importService;

    @Inject
    TradeService<Trade> irsService;

    @Inject
    @Named("portfolio")
    Transformer<SwapTrade> transformer;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio18-05-17v2-NPV.xlsx");

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new TradeUploadServiceImpl(accountService, portfolioService, irsService, transformer);
        importService.reload();
    }

    @Test
    public void testUploadOneIRS() {
        List<String> tradeIds = service.fromExcelNew(oneIRS.createInputStream());
        assertThat(tradeIds).isNotEmpty();
    }

    @Test
    public void testUploadAll() throws IOException {
        List<String> tradeIds = service.fromExcelNew(excel.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull();
    }

    @Test
    public void testHandleIRSOneRowUpdate() throws IOException {
        service.fromExcelNew(oneIRS.createInputStream());
        service.fromExcelNew(oneIRS.createInputStream());
        Iterable<Trade> irses = irsService.findAll();
        assertThat(irses).isNotEmpty().hasSize(2);
    }

    @Test
    public void testConcurrentUpload() {
        new MultithreadingTester().numThreads(10).numRoundsPerThread(1).add(() -> {
            service.fromExcelNew(oneIRS.createInputStream());
            Thread.sleep(1000);
            return null;
        }).run();
    }

    @Test
    public void testCompareVersion()
    {
        List<String> tradeIds = service.fromExcelNew(excel.createInputStream());
        Iterator<Trade> olds = irsService.findAll(2).iterator();
        importService.reload();
        service.fromExcelNew(excel.createInputStream());
        while(olds.hasNext())
        {
            Trade versionOld = olds.next();
            Trade versionNew = irsService.find(versionOld.getTradeId(), 2);

            if(!(versionNew instanceof FRA) && !versionOld.equals(versionNew))
            {
                log.info("old:" + versionOld.toString());
                log.info("new:" + versionNew.toString());
                break;
            }
            else
            {
                log.info("trade match:" + versionOld.getTradeId());
            }
        }

    }

    @Test
    public void testUploadAllNew() throws IOException {
        List<String> tradeIds = service.fromExcelNew(excel.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull();
    }

}