package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.TradingAccount;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
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
    private final TradingAccountService accountService;

    @Inject
    public TradeUploadServiceImpl(TradeService<IRS> irsService, TradeService<FRA> fraService, TradingAccountService accountService) {
        this.irsService = irsService;
        this.fraService = fraService;
        this.accountService = accountService;
    }

    public boolean uploadTradesFromExcel(InputStream fis) {
        try {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet("IRS-Cleared");
            Map<String, TradingAccount> accounts = new HashMap<>();
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    TradingAccount account = handleIRSRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("FRA-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    TradingAccount account = handleFRARow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("OIS-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    TradingAccount account = handleOISRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            sheet = workbook.getSheet("IRS-Bilateral");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    TradingAccount account = handleIRSBilateralRow(row);
                    accounts.putIfAbsent(account.getAccountId(), account);
                }
            }

            for (TradingAccount account : accounts.values()) {
                accountService.createOrUpdate(account);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return true;
    }

    private TradingAccount addToAccount(Row row, Trade trade) {
        TradingAccount account = accountService.findById(row.getCell(1).getStringCellValue());
        account.add(trade);
        return account;
    }

    private TradingAccount handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved IRS {}", irs);
        return account;
    }

    private TradingAccount handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        TradingAccount account = addToAccount(row, fra);
        log.debug("saved FRA {}", fra);
        return account;
    }

    private TradingAccount handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved OIS {}", irs);
        return account;
    }

    private TradingAccount handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved IRS-Bilateral {}", irs);
        return account;
    }
}
