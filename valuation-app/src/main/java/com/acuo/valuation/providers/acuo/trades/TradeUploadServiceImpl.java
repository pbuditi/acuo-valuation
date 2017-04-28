package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
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
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class TradeUploadServiceImpl implements TradeUploadService {

    private static Object lock = new Object();
    final SwapExcelParser parser = new SwapExcelParser();
    private final TradingAccountService accountService;
    private final PortfolioService portfolioService;
    private final TradeService<Trade> tradeService;

    @Inject
    public TradeUploadServiceImpl(TradingAccountService accountService,
                                  PortfolioService portfolioService,
                                  TradeService<Trade> tradeService) {
        this.accountService = accountService;
        this.portfolioService = portfolioService;
        this.tradeService = tradeService;
    }

    public List<String> uploadTradesFromExcel(InputStream fis) {
        List<Trade> tradeIdList = new ArrayList<>();
        try {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet("IRS-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    tradeIdList.add(handleIRSRow(row));
                }
            }

            sheet = workbook.getSheet("FRA-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    tradeIdList.add(handleFRARow(row));
                }
            }

            sheet = workbook.getSheet("OIS-Cleared");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    tradeIdList.add(handleOISRow(row));
                }
            }

            sheet = workbook.getSheet("IRS-Bilateral");
            if (sheet != null) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    tradeIdList.add(handleIRSBilateralRow(row));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        synchronized(lock){
            tradeService.createOrUpdate(tradeIdList);
        }

        return tradeIdList.stream().map(Trade::getTradeId).collect(toList());
    }

    private void linkPortfolio(Trade trade, String portfolioId) {
        if(log.isDebugEnabled()) {
            log.debug("linking to portfolioId: {}", portfolioId);
        }
        Portfolio portfolio = portfolioService.find(PortfolioId.fromString(portfolioId));
        trade.setPortfolio(portfolio);
    }

    private void linkAccount(Trade trade, String accountId) {
        if(log.isDebugEnabled()) {
            log.debug("linking to accountId: {}", accountId);
        }
        TradingAccount account = accountService.find(accountId);
        trade.setAccount(account);
    }

    private Trade handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        String accountId = row.getCell(1).getStringCellValue();
        String portfolioId = row.getCell(47).getStringCellValue();
        return handleTrade(irs, accountId, portfolioId);
    }

    private Trade handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        String accountId = row.getCell(1).getStringCellValue();
        String portfolioId = row.getCell(34).getStringCellValue();
        return handleTrade(fra, accountId, portfolioId);
    }

    private Trade handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        String accountId = row.getCell(1).getStringCellValue();
        String portfolioId = row.getCell(48).getStringCellValue();
        return handleTrade(irs, accountId, portfolioId);
    }

    private Trade handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        String accountId = row.getCell(1).getStringCellValue();
        String portfolioId = row.getCell(41).getStringCellValue();
        return handleTrade(irs, accountId, portfolioId);
    }

    private Trade handleTrade(Trade trade, String accountId, String portfolioId) {
        linkAccount(trade, accountId);
        linkPortfolio(trade, portfolioId);
        if(log.isDebugEnabled()) {
            log.debug("saved trade {}", trade);
        }
        return trade;
    }
}