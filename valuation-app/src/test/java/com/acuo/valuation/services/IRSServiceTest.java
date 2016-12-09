package com.acuo.valuation.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.MappingModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, Neo4jPersistModule.class})
@Slf4j
public class IRSServiceTest {

    IRSServiceImpl service;

    @Inject
    Neo4jPersistService session;

    FileInputStream fis;

    @Before
    public void setup() throws FileNotFoundException {
        service = new IRSServiceImpl(session);
        fis = new FileInputStream("src/test/resources/excel/NewExposures.xlsx");
    }

    @Test
    public void testHandleIRSRowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        log.debug("service {} row {}", service, row);
        service.handleIRSRow(row);
    }

    @Test
    public void testHandleFRARowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(1);
        service.handleFRARow(row);
    }

    @Test
    public void testHandleOIS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(2);
        Row row = sheet.getRow(1);
        service.handleOISRow(row);

    }

}
