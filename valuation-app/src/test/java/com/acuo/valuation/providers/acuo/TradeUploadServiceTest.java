package com.acuo.valuation.providers.acuo;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
@Slf4j
public class TradeUploadServiceTest {

    TradeUploadServiceImpl service;

    @Inject
    TradeService<IRS> irsService;

    @Inject
    TradeService<FRA> fraService;

    @Inject
    TradingAccountService accountService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ImportService importService;

    @Mock
    PricingService pricingService;

    @Mock
    MarkitValautionsProcessor processor;

    @Mock
    PricingResults pricingResults;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Before
    public void setup() throws FileNotFoundException {
        MockitoAnnotations.initMocks(this);
        service = new TradeUploadServiceImpl(irsService, fraService, accountService, portfolioService, pricingService, processor);
        importService.reload();

    }

    @Test
    public void testUploadOneIRS() {
        service.uploadTradesFromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testUploadAll() throws IOException {
        when(pricingService.priceSwapTrades(any(List.class))).thenReturn(pricingResults);
        when(processor.process(pricingResults)).thenReturn(Collections.emptyList());
        service.uploadTradesFromExcel(excel.createInputStream());    }

    @Test
    public void testHandleIRSOneRowUpdate() throws IOException {
        service.uploadTradesFromExcel(oneIRS.createInputStream());
        service.uploadTradesFromExcel(oneIRS.createInputStream());

        Iterable<IRS> irses = irsService.findAll();
        int count = 0;
        for (IRS irs : irses) {
            log.debug(irs.getId() + " id of irs");
            count ++;
        }

        Assert.assertTrue(count == 2);
    }
}