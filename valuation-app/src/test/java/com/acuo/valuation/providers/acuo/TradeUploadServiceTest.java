package com.acuo.valuation.providers.acuo;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.DataImporter;
import com.acuo.persist.core.DataLoader;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.MappingModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
        MappingModule.class,
        Neo4jPersistModule.class,
        RepositoryModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,})
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

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Before
    public void setup() throws FileNotFoundException {
        service = new TradeUploadServiceImpl(irsService, fraService, accountService, portfolioService);
        //importService.reload();
    }

    @Test
    public void testUploadOneIRS() {
        service.uploadTradesFromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testUploadAll() throws IOException {
        service.uploadTradesFromExcel(excel.createInputStream());
    }

    @Test
    public void testHandleIRSOneRowUpdate() throws FileNotFoundException, IOException {
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