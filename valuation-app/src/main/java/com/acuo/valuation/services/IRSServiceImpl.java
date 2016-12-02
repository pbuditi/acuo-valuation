package com.acuo.valuation.services;

import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.FRA;
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

    SwapExcelParser parser = new SwapExcelParser();

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

            for(int i = 1; i < sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                handleIRSRow(row);
            }

            sheet = workbook.getSheetAt(1);
            for(int i = 1; i < sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                handleFRARow(row);
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }

    public void handleIRSRow(Row row)
    {
        IRS irs = parser.buildIRS(row);
        Iterable<IRS> list = sessionProvider.get().query(IRS.class, "MATCH (n:IRS {id:\"" + irs.getIrsId() + "\"}) RETURN n", Collections.emptyMap());
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

    public void handleFRARow(Row row)
    {
        FRA fra = parser.buildFRA(row);
        Iterable<FRA> list = sessionProvider.get().query(FRA.class, "MATCH (n:FRA {id:\"" + fra.getFraId() + "\"}) RETURN n", Collections.emptyMap());
        if(list.iterator().hasNext())
        {
            //update case
            FRA existed = sessionProvider.get().load(FRA.class, list.iterator().next().getId(), 2 );
            existed.setClearingDate(fra.getClearingDate());
            existed.setMaturity(fra.getMaturity());
            existed.setCurrency(fra.getCurrency());

            if(fra.getPayLegs().size() > 0)
            {
                for(Leg leg :existed.getPayLegs())
                    sessionProvider.get().delete(leg);
            }
            else
            if(fra.getReceiveLegs().size() > 0)
            {
                for(Leg leg :existed.getReceiveLegs())
                    sessionProvider.get().delete(leg);
            }

            existed.setPayLegs(fra.getPayLegs());
            existed.setReceiveLegs(fra.getReceiveLegs());
            sessionProvider.get().save(existed, 2);
        }
        else
            sessionProvider.get().save(fra, 2);
    }

}
