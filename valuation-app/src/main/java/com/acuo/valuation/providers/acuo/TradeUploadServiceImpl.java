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
import java.util.HashMap;
import java.util.Map;

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
            Map<String, Account> accounts = new HashMap<>();
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    Account account = handleIRSRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("FRA-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    Account account = handleFRARow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("OIS-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    Account account = handleOISRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("IRS-Bilateral");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    Account account = handleIRSBilateralRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            for (Account account : accounts.values()) {
                accountService.createOrUpdate(account);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return true;
    }

    private Account addToAccount(Row row, Trade trade) {
        Account account = accountService.findById(row.getCell(1).getStringCellValue());
        account.add(trade);
        return account;
    }

    private Account handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        Account account = addToAccount(row, irs);
        log.debug("saved IRS {}", irs);
        return account;
    }

    private Account handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        Account account = addToAccount(row, fra);
        log.debug("saved FRA {}", fra);
        return account;
    }

    private Account handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        Account account = addToAccount(row, irs);
        log.debug("saved OIS {}", irs);
        return account;
    }

    private Account handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        Account account = addToAccount(row, irs);
        log.debug("saved IRS-Bilateral {}", irs);
        return account;
    }
}
