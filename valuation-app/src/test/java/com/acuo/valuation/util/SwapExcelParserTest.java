package com.acuo.valuation.util;

import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.valuation.utils.SwapExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

@Slf4j
public class SwapExcelParserTest {

    private Workbook workbook;
    private SwapExcelParser parser = new SwapExcelParser();

    @Before
    public void setUp() throws Exception
    {
        FileInputStream fis = new FileInputStream("src/test/resources/excel/NewExposures.xlsx");
        workbook = new XSSFWorkbook(fis);

    }

    @Test
    public void tesParserIRSCleared()
    {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);

       //IRS irs  = parser.buildIRS(row);
        //log.debug(irs.toString());
    }

    @Test
    public void tesParserFRACleared()
    {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(1);

        FRA fra = parser.buildFRA(row);
        log.debug(fra.toString());
    }

    @Test
    public void testParserOISCleared()
    {
        Sheet sheet = workbook.getSheetAt(2);
        Row row = sheet.getRow(1);

        //IRS irs = parser.buildOIS(row);
        //log.debug(irs.toString());
    }

    @Test
    public void tesParserIRSBilateral()
    {
        Sheet sheet = workbook.getSheetAt(3);
        Row row = sheet.getRow(1);

        //IRS irs = parser.buildIRSBilateral(row);
        //log.debug(irs.toString());
    }
}
