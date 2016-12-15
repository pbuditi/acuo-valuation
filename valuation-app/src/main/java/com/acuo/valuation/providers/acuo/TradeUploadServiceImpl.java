package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.services.FRAService;
import com.acuo.persist.services.IRSService;
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
    private final IRSService irsService;
    private final FRAService fraService;

    @Inject
    public TradeUploadServiceImpl(IRSService irsService, FRAService fraService) {
        this.irsService = irsService;
        this.fraService = fraService;
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

    public void handleIRSRow(Row row) {
        IRS irs = parser.buildIRS(row);
        log.debug("parsed IRS {}", irs);
        irs = irsService.createOrUpdateById(irs, irs.getIrsId());
        log.debug("saved IRS {}", irs.getId());
    }

    public void handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        log.debug("parsed IRS {}", fra);
        fra = fraService.createOrUpdateById(fra, fra.getFraId());
        log.debug("saved IRS {}", fra);

    }

    public void handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        log.debug("parsed IRS {}", irs);
        irs = irsService.createOrUpdateById(irs, irs.getIrsId());
        log.debug("saved IRS {}", irs);
    }

    public void handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        log.debug("parsed IRS {}", irs);
        irs = irsService.createOrUpdateById(irs,irs.getIrsId());
        log.debug("saved IRS {}", irs);
    }
}
