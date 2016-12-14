package com.acuo.valuation.providers.acuo;

import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.utils.SwapExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Collections;

@Slf4j
public class TradeUploadServiceImpl implements TradeUploadService {

    private Neo4jPersistService sessionProvider;

    SwapExcelParser parser = new SwapExcelParser();

    @Inject
    public TradeUploadServiceImpl(Neo4jPersistService sessionProvider) {
        this.sessionProvider = sessionProvider;
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
        Iterable<IRS> list = sessionProvider.get().query(IRS.class, "MATCH (n:IRS {id:\"" + irs.getIrsId() + "\"}) RETURN n", Collections.emptyMap());
        if (list.iterator().hasNext()) {
            IRS existed = sessionProvider.get().load(IRS.class, list.iterator().next().getId(), 2);
            log.debug("updating irs {} ", existed);
            existed.setClearingDate(irs.getClearingDate());
            existed.setMaturity(irs.getMaturity());
            existed.setTradeType(irs.getTradeType());

            log.debug("pay legs {} ", existed.getPayLegs() );
            if (existed.getPayLegs() != null) {
                for (Leg leg : existed.getPayLegs())
                    sessionProvider.get().delete(leg);
            }

            log.debug("receive legs {} ", existed.getReceiveLegs() );
            if (existed.getReceiveLegs() != null) {
                for (Leg leg : existed.getReceiveLegs())
                    sessionProvider.get().delete(leg);
            }
            existed.setPayLegs(irs.getPayLegs());
            existed.setReceiveLegs(irs.getReceiveLegs());
            sessionProvider.get().save(existed, 2);
        } else {
            log.debug("save irs {} into the DB", irs);
            sessionProvider.get().save(irs, 2);
        }
    }

    public void handleFRARow(Row row) {
        FRA fra = parser.buildFRA(row);
        Iterable<FRA> list = sessionProvider.get().query(FRA.class, "MATCH (n:FRA {id:\"" + fra.getFraId() + "\"}) RETURN n", Collections.emptyMap());
        if (list.iterator().hasNext()) {
            //update case
            FRA existed = sessionProvider.get().load(FRA.class, list.iterator().next().getId(), 2);
            log.debug("updating fra {} ", existed);
            existed.setClearingDate(fra.getClearingDate());
            existed.setMaturity(fra.getMaturity());
            existed.setCurrency(fra.getCurrency());
            existed.setTradeType(fra.getTradeType());

            if (fra.getPayLegs().size() > 0) {
                if(existed.getPayLegs() != null) {
                    for (Leg leg : existed.getPayLegs())
                        sessionProvider.get().delete(leg);
                }
            } else if (fra.getReceiveLegs().size() > 0) {
                if(existed.getReceiveLegs() != null) {
                    for (Leg leg : existed.getReceiveLegs())
                        sessionProvider.get().delete(leg);
                }
            }

            existed.setPayLegs(fra.getPayLegs());
            existed.setReceiveLegs(fra.getReceiveLegs());
            sessionProvider.get().save(existed, 2);
        } else {
            log.debug("save fra {} into the DB", fra);
            sessionProvider.get().save(fra, 2);
        }
    }

    public void handleOISRow(Row row) {
        IRS irs = parser.buildOIS(row);
        Iterable<IRS> list = sessionProvider.get().query(IRS.class, "MATCH (n:IRS {id:\"" + irs.getIrsId() + "\"}) RETURN n", Collections.emptyMap());
        if (list.iterator().hasNext()) {
            //update case
            IRS existed = sessionProvider.get().load(IRS.class, list.iterator().next().getId(), 2);
            log.debug("updating OIS {} ", existed);
            existed.setClearingDate(irs.getClearingDate());
            existed.setMaturity(irs.getMaturity());
            existed.setTradeType(irs.getTradeType());

            if (existed.getPayLegs() != null)
                for (Leg leg : existed.getPayLegs())
                    sessionProvider.get().delete(leg);
            if (existed.getReceiveLegs() != null)
                for (Leg leg : existed.getReceiveLegs())
                    sessionProvider.get().delete(leg);
            existed.setPayLegs(irs.getPayLegs());
            existed.setReceiveLegs(irs.getReceiveLegs());
            sessionProvider.get().save(existed, 2);
        } else {
            log.debug("save ois {} into the DB", irs);
            sessionProvider.get().save(irs, 2);
        }
    }

    public void handleIRSBilateralRow(Row row) {
        IRS irs = parser.buildIRSBilateral(row);
        Iterable<IRS> list = sessionProvider.get().query(IRS.class, "MATCH (n:IRS {id:\"" + irs.getIrsId() + "\"}) RETURN n", Collections.emptyMap());
        if (list.iterator().hasNext()) {
            IRS existed = sessionProvider.get().load(IRS.class, list.iterator().next().getId(), 2);
            log.debug("updating irs {} ", existed);
            existed.setClearingDate(irs.getClearingDate());
            existed.setMaturity(irs.getMaturity());
            existed.setTradeType(irs.getTradeType());
            log.debug("pay legs {} ", existed.getPayLegs() );
            if (existed.getPayLegs() != null) {
                for (Leg leg : existed.getPayLegs())
                    sessionProvider.get().delete(leg);
            }

            log.debug("receive legs {} ", existed.getReceiveLegs() );
            if (existed.getReceiveLegs() != null) {
                for (Leg leg : existed.getReceiveLegs())
                    sessionProvider.get().delete(leg);
            }
            existed.setPayLegs(irs.getPayLegs());
            existed.setReceiveLegs(irs.getReceiveLegs());
            sessionProvider.get().save(existed, 2);
        } else {
            log.debug("save irs {} into the DB", irs);
            sessionProvider.get().save(irs, 2);
        }
    }

}
