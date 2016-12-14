package com.acuo.valuation.providers.acuo;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.providers.acuo.TradeUploadServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, Neo4jPersistModule.class})
@Slf4j
public class TradeUploadServiceTest {

    TradeUploadServiceImpl service;

    @Inject
    Neo4jPersistService session;

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/NewExposures.xlsx");

    @Before
    public void setup() throws FileNotFoundException {
        service = new TradeUploadServiceImpl(session);
    }

    @Test
    public void testHandleIRSRowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        log.debug("service {} row {}", service, row);
        service.handleIRSRow(row);
    }

    @Test
    public void testHandleFRARowS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(1);
        service.handleFRARow(row);
    }

    @Test
    public void testHandleOIS() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(2);
        Row row = sheet.getRow(1);
        service.handleOISRow(row);
    }

    @Test
    public void testHandleIRSBilateral() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(excel.createInputStream());
        Sheet sheet = workbook.getSheetAt(3);
        Row row = sheet.getRow(1);
        service.handleIRSBilateralRow(row);
    }
}
