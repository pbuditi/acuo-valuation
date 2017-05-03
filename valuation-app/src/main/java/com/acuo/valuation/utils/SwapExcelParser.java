package com.acuo.valuation.utils;

import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.enums.PricingProvider;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.index.FloatingRateType;
import com.opengamma.strata.basics.index.ImmutableFloatingRateName;
import com.opengamma.strata.basics.schedule.Frequency;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public class SwapExcelParser {

    public static String TRADE_TYPE_CLEARD = "Cleared";

    public static String TRADE_TYPE_BILATERAL = "Bilateral";

    public IRS buildIRS(Row row) {
        try {
            IRS irs = new IRS();
            irs.setCurrency(Currency.parse(row.getCell(4).getStringCellValue()));
            irs.setTradeDate(dateToLocalDate(row.getCell(5).getDateCellValue()));
            irs.setMaturity(dateToLocalDate(row.getCell(6).getDateCellValue()));
            irs.setClearingDate(dateToLocalDate(row.getCell(7).getDateCellValue()));
            irs.setTradeId(row.getCell(3).getStringCellValue());
            irs.setTradeType(TRADE_TYPE_CLEARD);
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Clarus);
            irs.setPricingSource(pricingSource);


            log.debug("loading irs id {} ", irs.getTradeId());

            String leg1Relationship = row.getCell(41).getStringCellValue();
            String leg2Relationship = row.getCell(42).getStringCellValue();

            Leg leg1 = buildLeg(row, 15);
            Leg leg2 = buildLeg(row, 28);

            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);

            if (leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);

            if (leg2Relationship != null && leg2Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg2);
            else
                payLegs.add(leg2);

            return irs;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Leg buildLeg(Row row, int startIndex) {
        Leg leg = new Leg();
        leg.setLegId(row.getCell(3).getStringCellValue() + "-" + startIndex);
        leg.setType(getStringValue(row.getCell(startIndex)));
        leg.setCurrency(Currency.parse(getStringValue(row.getCell(startIndex + 1))));
        leg.setPaymentFrequency(Frequency.parse(getStringValue(row.getCell(startIndex + 2))));
        leg.setBusinessDayConvention(BusinessDayConvention.of(getStringValue(row.getCell(startIndex + 3))));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 4)));
        leg.setDayCount(DayCount.of(getStringValue(row.getCell(startIndex + 5))));
        String index = getStringValue(row.getCell(startIndex + 6));
        if (index != null) {
            try{
                leg.setIndex(FloatingRateName.of(index));
            } catch (Exception e) {
                log.warn(e.getMessage());
                leg.setIndex(ImmutableFloatingRateName.of(index,index, FloatingRateType.IBOR));
            }
        }
        String tenor = getStringValue(row.getCell(startIndex + 7));
        if (tenor != null) leg.setIndexTenor(Tenor.parse(tenor));
        String frequency = getStringValue(row.getCell(startIndex + 8));
        if(frequency != null) leg.setResetFrequency(Frequency.parse(frequency));
        if (row.getCell(startIndex + 9) != null)
            leg.setPayStart(dateToLocalDate(row.getCell(startIndex + 9).getDateCellValue()));
        if (row.getCell(startIndex + 9) != null)
            leg.setPayEnd(dateToLocalDate(row.getCell(startIndex + 10).getDateCellValue()));
        if (row.getCell(startIndex + 11) != null)
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 11)).replace(",", "")));
        if (row.getCell(startIndex + 12) != null)
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 12).getStringCellValue())) / 100);
        return leg;
    }

    private String getStringValue(Cell cell) {
        if (cell == null)
            return null;
        else
            return cell.getStringCellValue();
    }


    public FRA buildFRA(Row row) {
        FRA fra = new FRA();

        try {

            fra.setTradeId(row.getCell(3).getStringCellValue());
            fra.setCurrency(Currency.parse(row.getCell(4).getStringCellValue()));
            fra.setTradeDate(dateToLocalDate(row.getCell(5).getDateCellValue()));
            fra.setMaturity(dateToLocalDate(row.getCell(6).getDateCellValue()));
            fra.setClearingDate(dateToLocalDate(row.getCell(7).getDateCellValue()));
            fra.setTradeType(TRADE_TYPE_CLEARD);
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Clarus);
            fra.setPricingSource(pricingSource);

            Leg leg1 = buildFraLeg(row, 16);

            String leg1Relationship = row.getCell(9).getStringCellValue();

            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            fra.setPayLegs(payLegs);
            fra.setReceiveLegs(receiveLegs);

            if (leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fra;
    }

    private Leg buildFraLeg(Row row, int startIndex) {
        Leg leg = new Leg();
        leg.setType(getStringValue(row.getCell(startIndex)));
        leg.setCurrency(Currency.parse(getStringValue(row.getCell(startIndex + 1))));
        leg.setBusinessDayConvention(BusinessDayConvention.of(getStringValue(row.getCell(startIndex + 2))));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 3)));
        leg.setDayCount(DayCount.of(getStringValue(row.getCell(startIndex + 4))));
        leg.setIndex(FloatingRateName.of(getStringValue(row.getCell(startIndex + 5))));
        leg.setIndexTenor(Tenor.parse(getStringValue(row.getCell(startIndex + 6))));
        if (row.getCell(startIndex + 7) != null)
            leg.setPayStart(dateToLocalDate(row.getCell(startIndex + 7).getDateCellValue()));
        if (row.getCell(startIndex + 8) != null)
            leg.setPayEnd(dateToLocalDate(row.getCell(startIndex + 8).getDateCellValue()));
        if (row.getCell(startIndex + 9) != null)
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 9)).replace(",", "")));
        if (row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_NUMERIC))
            leg.setFixedRate(row.getCell(startIndex + 10).getNumericCellValue() / 100);
        else if (row.getCell(startIndex + 10) != null && row.getCell(startIndex + 10).getCellStyle().equals(Cell.CELL_TYPE_STRING))
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 10).getStringCellValue())) / 100);
        return leg;
    }


    public IRS buildOIS(Row row) {
        try {
            IRS irs = new IRS();
            irs.setCurrency(Currency.parse(row.getCell(4).getStringCellValue()));
            irs.setTradeDate(dateToLocalDate(row.getCell(5).getDateCellValue()));
            irs.setMaturity(dateToLocalDate(row.getCell(6).getDateCellValue()));
            irs.setClearingDate(dateToLocalDate(row.getCell(7).getDateCellValue()));

            irs.setTradeId(row.getCell(3).getStringCellValue());
            irs.setTradeType(TRADE_TYPE_CLEARD);
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Clarus);
            irs.setPricingSource(pricingSource);


            log.debug("irs trade id {}",irs.getTradeId());

            Leg leg1 = buildOISLeg(row, 15);
            Leg leg2 = buildOISLeg(row, 28);

            String leg1Relationship = row.getCell(41).getStringCellValue();
            String leg2Relationship = row.getCell(42).getStringCellValue();


            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);

            if (leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);

            if (leg2Relationship != null && leg2Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg2);
            else
                payLegs.add(leg2);

            return irs;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Leg buildOISLeg(Row row, int startIndex) {
        Leg leg = new Leg();
        leg.setType(getStringValue(row.getCell(startIndex)));
        if(row.getCell(startIndex + 1) != null)
        leg.setIndexTenor(Tenor.parse(getStringValue(row.getCell(startIndex + 1))));
        leg.setCurrency(Currency.parse(getStringValue(row.getCell(startIndex + 2))));
        leg.setBusinessDayConvention(BusinessDayConvention.of(getStringValue(row.getCell(startIndex + 4))));
        leg.setRefCalendar(getStringValue(row.getCell(startIndex + 5)));
        leg.setPaymentFrequency(Frequency.parse(getStringValue(row.getCell(startIndex + 3))));
        leg.setDayCount(DayCount.of(getStringValue(row.getCell(startIndex + 6))));
        String index = getStringValue(row.getCell(startIndex + 7));
        if(index != null) leg.setIndex(FloatingRateName.of(index));
        String frequency = getStringValue(row.getCell(startIndex + 8));
        if(frequency != null) leg.setResetFrequency(Frequency.parse(frequency));
        if (row.getCell(startIndex + 9) != null)
            leg.setPayStart(dateToLocalDate(row.getCell(startIndex + 9).getDateCellValue()));
        if (row.getCell(startIndex + 10) != null)
            leg.setPayEnd(dateToLocalDate(row.getCell(startIndex + 10).getDateCellValue()));
        if (row.getCell(startIndex + 11) != null)
            leg.setNotional(Double.parseDouble(getStringValue(row.getCell(startIndex + 11)).replace(",", "")));
        if (row.getCell(startIndex + 12) != null)
            leg.setFixedRate(Double.parseDouble((row.getCell(startIndex + 12).getStringCellValue())) / 100);
        return leg;
    }


    public IRS buildIRSBilateral(Row row) {
        try {

            IRS irs = new IRS();
            irs.setCurrency(Currency.parse(row.getCell(4).getStringCellValue()));
            irs.setTradeDate(dateToLocalDate(row.getCell(5).getDateCellValue()));
            irs.setMaturity(dateToLocalDate(row.getCell(6).getDateCellValue()));
            irs.setClearingDate(dateToLocalDate(row.getCell(7).getDateCellValue()));

            irs.setTradeId(row.getCell(3).getStringCellValue());
            irs.setTradeType(TRADE_TYPE_BILATERAL);
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Markit);
            irs.setPricingSource(pricingSource);

            log.debug("loading irs Bilateral id {} ", irs.getTradeId());

            Leg leg1 = buildLeg(row, 11);
            Leg leg2 = buildLeg(row, 24);

            String leg1Relationship = row.getCell(37).getStringCellValue();
            String leg2Relationship = row.getCell(38).getStringCellValue();


            Set<Leg> payLegs = new HashSet<Leg>();
            Set<Leg> receiveLegs = new HashSet<Leg>();

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);

            if (leg1Relationship != null && leg1Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg1);
            else
                payLegs.add(leg1);

            if (leg2Relationship != null && leg2Relationship.equalsIgnoreCase("R"))
                receiveLegs.add(leg2);
            else
                payLegs.add(leg2);

            return irs;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private static LocalDate dateToLocalDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    private static int sign(String direction) {
        return "R".equals(direction) ? 1 : -1;
    }
}
