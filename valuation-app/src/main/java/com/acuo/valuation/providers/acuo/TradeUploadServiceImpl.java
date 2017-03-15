package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.utils.SwapExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TradeUploadServiceImpl implements TradeUploadService {

    final SwapExcelParser parser = new SwapExcelParser();
    private final TradeService<IRS> irsService;
    private final TradeService<FRA> fraService;
    private final TradingAccountService accountService;
    private final PortfolioService portfolioService;
    private final PricingService pricingService;
    private final MarkitValautionsProcessor resultProcessor;
    private final List<String> tradeIdList = new ArrayList<String>();

    @Inject
    public TradeUploadServiceImpl(TradeService<IRS> irsService,
                                  TradeService<FRA> fraService,
                                  TradingAccountService accountService,
                                  PortfolioService portfolioService,
                                  PricingService pricingService,
                                  MarkitValautionsProcessor resultProcessor) {
        this.irsService = irsService;
        this.fraService = fraService;
        this.accountService = accountService;
        this.portfolioService = portfolioService;
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
    }

    public List<String> uploadTradesFromExcel(InputStream fis) {
        MarginCallDetail marginCallDetail = null;
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

        return tradeIdList;
    }

    private TradingAccount addToAccount(Row row, Trade trade) {
        TradingAccount account = accountService.findById(row.getCell(1).getStringCellValue());
        account.add(trade);
        return account;
    }

    private void linkPortfolio(Trade trade, String portfolioId)
    {
        log.info("portfolioId:" + portfolioId);
        Portfolio portfolio = portfolioService.findById(portfolioId);
        trade.setPortfolio(portfolio);
    }

    private TradingAccount handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        linkPortfolio(irs, row.getCell(47).getStringCellValue());
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved IRS {}", irs);
        tradeIdList.add(irs.getTradeId());
        return account;
    }

    private TradingAccount handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        linkPortfolio(fra, row.getCell(34).getStringCellValue());
        TradingAccount account = addToAccount(row, fra);
        log.debug("saved FRA {}", fra);
        return account;
    }

    private TradingAccount handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        linkPortfolio(irs, row.getCell(46).getStringCellValue());
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved OIS {}", irs);
        tradeIdList.add(irs.getTradeId());
        return account;
    }

    private TradingAccount handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        linkPortfolio(irs, row.getCell(41).getStringCellValue());
        TradingAccount account = addToAccount(row, irs);
        log.debug("saved IRS-Bilateral {}", irs);
        tradeIdList.add(irs.getTradeId());
        return account;
    }
}
