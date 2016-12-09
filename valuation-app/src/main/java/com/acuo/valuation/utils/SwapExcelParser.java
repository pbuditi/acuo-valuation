package com.acuo.valuation.utils;

import com.acuo.persist.entity.Account;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SwapExcelParser {

    public IRS buildIRS(Row row)
    {
        IRS irs = null;
        try
        {
            irs = new IRS();

            Account account = new Account();
            account.setAccountId(row.getCell(1).getStringCellValue());
            irs.setAccount(account);

            irs.setMaturity(row.getCell(6).getDateCellValue());
            irs.setClearingDate(row.getCell(7).getDateCellValue());

            irs.setIrsId((new Double(row.getCell(3).getNumericCellValue())).intValue() + "");



            log.debug("loading irs id {} ", irs.getIrsId());

            Leg leg1 = buildLeg(row, 15);
            Leg leg2 = buildLeg(row, 28);

            String leg1Relationship = row.getCell(41).getStringCellValue();
            String leg2Relationship = row.getCell(42).getStringCellValue();


            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);

            if(leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);

            if(leg2Relationship != null && leg2Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg2);
            else
                payLegs.add(leg2);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return irs;
    }

    private Leg buildLeg(Row row, int startIndex)
    {
        Leg leg = new Leg();
        leg.setType(getStringValue(row.getCell(startIndex)));
        leg.setCurrency(getStringValue(row.getCell(startIndex + 1)));
        leg.setBusinessDayConvention(getStringValue(row.getCell(startIndex + 3)));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 4)));
        leg.setPaymentFrequency(getStringValue(row.getCell(startIndex + 2)));
        leg.setDayCount(getStringValue(row.getCell(startIndex + 5)));
        leg.setIndex(getStringValue(row.getCell(startIndex+ 6)));
        leg.setIndexTenor(getStringValue(row.getCell(startIndex + 7)));
        leg.setResetFrequency(getStringValue(row.getCell(startIndex + 8)));
        if(row.getCell(startIndex + 9) != null)
            leg.setPayStart(row.getCell(startIndex + 9).getDateCellValue());
        if (row.getCell(startIndex + 9) != null)
            leg.setPayEnd(row.getCell(startIndex + 10).getDateCellValue());
        if(row.getCell(startIndex + 11) != null)
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 11)).replace(",", "")));
        if(row.getCell(startIndex + 12) != null && row.getCell(startIndex + 12).getCellStyle().equals(Cell.CELL_TYPE_NUMERIC))
            leg.setFixedRate(row.getCell(startIndex + 12).getNumericCellValue());
        else
        if(row.getCell(startIndex + 12) != null && row.getCell(startIndex + 12).getCellStyle().equals(Cell.CELL_TYPE_STRING))
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 12).getStringCellValue())));
        return leg;
    }

    private String getStringValue(Cell cell)
    {
        if(cell == null)
            return null;
        else
            return cell.getStringCellValue();
    }


    public FRA buildFRA(Row row)
    {
        FRA fra = new FRA();

        try {

            Account account = new Account();
            account.setAccountId(row.getCell(1).getStringCellValue());
            fra.setAccount(account);

            fra.setFraId((new Double(row.getCell(3).getNumericCellValue())).intValue() + "");
            fra.setCurrency(row.getCell(4).getStringCellValue());
            fra.setMaturity(row.getCell(6).getDateCellValue());
            fra.setClearingDate(row.getCell(7).getDateCellValue());

            Leg leg1 = buildFraLeg(row, 16);

            String leg1Relationship = row.getCell(9).getStringCellValue();

            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            fra.setPayLegs(payLegs);
            fra.setReceiveLegs(receiveLegs);

            if(leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);




        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return fra;
    }

    private Leg buildFraLeg(Row row, int startIndex)
    {
        Leg leg = new Leg();
        leg.setType(getStringValue(row.getCell(startIndex)));
        leg.setCurrency(getStringValue(row.getCell(startIndex + 1)));
        leg.setBusinessDayConvention(getStringValue(row.getCell(startIndex + 2)));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 3)));
        leg.setDayCount(getStringValue(row.getCell(startIndex + 4)));
        leg.setIndex(getStringValue(row.getCell(startIndex+ 5)));
        leg.setIndexTenor(getStringValue(row.getCell(startIndex + 6)));
        if(row.getCell(startIndex + 7) != null)
            leg.setPayStart(row.getCell(startIndex + 7).getDateCellValue());
        if (row.getCell(startIndex + 8) != null)
            leg.setPayEnd(row.getCell(startIndex + 8).getDateCellValue());
        if(row.getCell(startIndex + 9) != null)
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 9)).replace(",", "")));
        if(row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_NUMERIC))
            leg.setFixedRate(row.getCell(startIndex + 10).getNumericCellValue());
        else
        if(row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_STRING))
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 10).getStringCellValue())));
        return leg;
    }


    public IRS buildOIS(Row row)
    {
        IRS irs = null;
        try
        {
            irs = new IRS();

            Account account = new Account();
            account.setAccountId(row.getCell(1).getStringCellValue());
            irs.setAccount(account);

            irs.setMaturity(row.getCell(6).getDateCellValue());
            irs.setClearingDate(row.getCell(7).getDateCellValue());

            irs.setIrsId((new Double(row.getCell(3).getNumericCellValue())).intValue() + "");



            log.debug(irs.getIrsId());

            Leg leg1 = buildOISLeg(row, 15);
            Leg leg2 = buildOISLeg(row, 27);

            String leg1Relationship = row.getCell(39).getStringCellValue();
            String leg2Relationship = row.getCell(40).getStringCellValue();


            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);

            if(leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);

            if(leg2Relationship != null && leg2Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg2);
            else
                payLegs.add(leg2);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return irs;
    }

    private Leg buildOISLeg(Row row, int startIndex)
    {
        Leg leg = new Leg();
        leg.setType(getStringValue(row.getCell(startIndex)));
        leg.setCurrency(getStringValue(row.getCell(startIndex + 1)));
        leg.setBusinessDayConvention(getStringValue(row.getCell(startIndex + 3)));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 4)));
        leg.setPaymentFrequency(getStringValue(row.getCell(startIndex + 2)));
        leg.setDayCount(getStringValue(row.getCell(startIndex + 5)));
        leg.setIndex(getStringValue(row.getCell(startIndex+ 6)));
        leg.setResetFrequency(getStringValue(row.getCell(startIndex + 7)));
        if(row.getCell(startIndex + 8) != null)
            leg.setPayStart(row.getCell(startIndex + 8).getDateCellValue());
        if (row.getCell(startIndex + 9) != null)
            leg.setPayEnd(row.getCell(startIndex + 9).getDateCellValue());
        if(row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_STRING))
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 10)).replace(",", "")));
        else
        if(row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_NUMERIC))
            leg.setNotional(row.getCell(startIndex + 10).getNumericCellValue());
        if(row.getCell(startIndex + 11) != null && row.getCell(startIndex + 11).getCellStyle().equals(Cell.CELL_TYPE_NUMERIC))
            leg.setFixedRate(row.getCell(startIndex + 11).getNumericCellValue());
        else
        if(row.getCell(startIndex + 11) != null && row.getCell(startIndex + 11).getCellStyle().equals(Cell.CELL_TYPE_STRING))
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 11).getStringCellValue())));
        return leg;
    }

}