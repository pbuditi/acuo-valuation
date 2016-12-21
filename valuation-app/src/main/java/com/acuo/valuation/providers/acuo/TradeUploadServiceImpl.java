package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.Account;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.AccountService;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.utils.SwapExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import java.io.InputStream;

@Slf4j
public class TradeUploadServiceImpl implements TradeUploadService {

    final SwapExcelParser parser = new SwapExcelParser();
    private final TradeService<IRS> irsService;
    private final TradeService<FRA> fraService;
    private final AccountService accountService;

    @Inject
    public TradeUploadServiceImpl(TradeService<IRS> irsService, TradeService<FRA> fraService, AccountService accountService) {
        this.irsService = irsService;
        this.fraService = fraService;
        this.accountService = accountService;
    }

    public boolean uploadTradesFromExcel(InputStream fis) {
        try {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet("IRS-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    handleIRSRow(row);
                }
            }

            sheet = workbook.getSheet("FRA-Cleared");
            if (sheet != null) {
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    handleFRARow(row);
                }
            }

            sheet = workbook.getSheet("OIS-Cleared");
            if (sheet != null) {
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    handleOISRow(row);
                }
            }

            sheet = workbook.getSheet("IRS-Bilateral");
            if (sheet != null) {
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    handleIRSBilateralRow(row);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return true;
    }

    private void addToAccount(Row row, Trade trade) {
        Account account = accountService.findById(row.getCell(1).getStringCellValue());
        account.add(trade);
        accountService.createOrUpdate(account);
    }

    public IRS handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        addToAccount(row, irs);
        log.debug("saved IRS {}", irs);
        return irs;
    }

    public FRA handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        addToAccount(row, fra);
        log.debug("saved FRA {}", fra);
        return fra;
    }

    public IRS handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        addToAccount(row, irs);
        log.debug("saved OIS {}", irs);
        return irs;
    }

    public IRS handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        addToAccount(row, irs);
        log.debug("saved IRS-Bilateral {}", irs);
        return irs;
    }
}
