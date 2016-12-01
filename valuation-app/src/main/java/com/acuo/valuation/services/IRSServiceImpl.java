package com.acuo.valuation.services;

import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.valuation.utils.SwapExcelParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.Collections;

@Slf4j
public class IRSServiceImpl implements IRSService {


    private Neo4jPersistService sessionProvider;

    @Inject
    public IRSServiceImpl(Neo4jPersistService sessionProvider)
    {
        this.sessionProvider = sessionProvider;
    }

    public boolean uploadIRS(FileInputStream fis)
    {
        try
        {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            SwapExcelParser parser = new SwapExcelParser();
            log.debug("row:" + sheet.getLastRowNum() + "");
            for(int i = 1; i < sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                IRS irs = parser.parserIRS(row);
                log.debug(irs.toString());
                Iterable<IRS> list = sessionProvider.get().query(IRS.class, "MATCH (n:IRS {irsId:\"" + irs.getIrsId() + "\"}) RETURN n", Collections.emptyMap());
                if(list.iterator().hasNext())
                {
                    //update case
                    IRS existed = sessionProvider.get().load(IRS.class, list.iterator().next().getId(), 2 );
                    existed.setClearingDate(irs.getClearingDate());
                    existed.setMaturity(irs.getMaturity());

                    for(Leg leg :existed.getPayLegs())
                        sessionProvider.get().delete(leg);
                    for(Leg leg :existed.getReceiveLegs())
                        sessionProvider.get().delete(leg);
                    existed.setPayLegs(irs.getPayLegs());
                    existed.setReceiveLegs(irs.getReceiveLegs());
                    sessionProvider.get().save(existed, 2);
                }
                else
                    sessionProvider.get().save(irs, 2);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }


}
