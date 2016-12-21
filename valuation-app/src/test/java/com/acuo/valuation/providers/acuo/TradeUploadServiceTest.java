package com.acuo.valuation.providers.acuo;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.DataImporter;
import com.acuo.persist.core.DataLoader;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.AccountService;
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
                                DataImporterModule.class})
@Slf4j
public class TradeUploadServiceTest {

    TradeUploadServiceImpl service;

    @Inject
    TradeService<IRS> irsService;

    @Inject
    TradeService<FRA> fraService;

    @Inject
    AccountService accountService;

    @Inject
    DataLoader dataLoader;

    @Inject
    DataImporter dataImporter;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/NewExposures.xlsx");

    @Before
    public void setup() throws FileNotFoundException {
        service = new TradeUploadServiceImpl(irsService, fraService, accountService);
        dataLoader.purgeDatabase();
        dataLoader.createConstraints();;
        dataImporter.importFiles("clients", "legalentities", "accounts");
    }

    @Test
    public void testUploadOneIRS() {
        service.uploadTradesFromExcel(oneIRS.createInputStream());
    }

    @Test
    public void testHandleIRSRowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            service.handleIRSRow(row);
        }
    }

    @Test
    public void testHandleIRSOneRowUpdate() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        service.handleIRSRow(row);
        service.handleIRSRow(row);
        Iterable<IRS> irses = irsService.findAll();
        int count = 0;
        for (IRS irs : irses) {
            log.debug(irs.getId() + " id of irs");
            count ++;
        }

        Assert.assertFalse(count != 1);
    }

    @Test
    public void testHandleFRARowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(1);
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            service.handleFRARow(row);
        }
    }

    @Test
    public void testHandleOIS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(2);
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            service.handleOISRow(row);
        }
    }

    @Test
    public void testHandleIRSBilateral() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(3);
        for (int i = 1; i < sheet.getLastRowNum(); i++)
        {
            Row row = sheet.getRow(i);
            service.handleIRSBilateralRow(row);
        }
    }
}