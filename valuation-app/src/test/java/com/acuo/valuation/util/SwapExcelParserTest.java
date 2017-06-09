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

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SwapExcelParserTest {

    private Workbook workbook;
    private SwapExcelParser parser = new SwapExcelParser();

    @Before
    public void setUp() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/excel/legacy/TradePortfolio.xlsx");
        workbook = new XSSFWorkbook(fis);
    }

    @Test
    public void tesParserIRSCleared() {
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1);
        IRS trade  = parser.buildIRS(row);
        assertThat(trade).isNotNull();
    }

    @Test
    public void tesParserFRACleared() {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(1);
        FRA trade = parser.buildFRA(row);
        assertThat(trade).isNotNull();
    }

    @Test
    public void testParserOISCleared() {
        Sheet sheet = workbook.getSheetAt(2);
        Row row = sheet.getRow(1);
        IRS trade = parser.buildOIS(row);
        assertThat(trade).isNotNull();
    }

    @Test
    public void tesParserIRSBilateral() {
        Sheet sheet = workbook.getSheetAt(3);
        Row row = sheet.getRow(1);
        IRS trade = parser.buildIRSBilateral(row);
        assertThat(trade).isNotNull();
    }
}
