package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.*;
import com.acuo.persist.entity.Currency;
import com.acuo.persist.services.*;
import com.acuo.valuation.services.MarginCallGenService;
import com.acuo.valuation.services.PricingService;
import com.opengamma.strata.basics.currency.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MarkitMarginCallGenServiceImpl implements MarginCallGenService {


    private final MarginStatementService marginStatementService;
    private final AgreementService agreementService;
    private final CurrencyService currencyService;



    DecimalFormat df = new DecimalFormat("#.##");
    Double pv = null;
    com.opengamma.strata.basics.currency.Currency currencyOfValue = null;

    @Inject
    public MarkitMarginCallGenServiceImpl(MarginStatementService marginStatementService, AgreementService agreementService, CurrencyService currencyService)
    {

        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    @Override
    public boolean geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation valuation)
    {
        //given a Agreeement, a Portofolio, a Valuation


        valuation.getValues().stream().filter(value -> value.getSource().equals("Markit")).forEach(value -> { pv = value.getPv(); currencyOfValue = value.getCurrency();});


        ClientSignsRelation clientSignsRelation = agreement.getClientSignsRelation();


        Double balance = clientSignsRelation.getVariationMarginBalance();
        Double pendingCollateral = clientSignsRelation.getVariationPending();

        if(!currencyOfValue.equals(agreement.getCurrency()))
            pv = getFXValue(currencyOfValue, agreement.getCurrency(), pv);

        if(clientSignsRelation.getThreshold() != null && Math.abs(pv) > clientSignsRelation.getThreshold())
            return false;

        log.info("pv:" + pv);
        log.info("balance:" + balance);
        log.info("pendingCollateral:" + pendingCollateral);
        Double diff = pv - (balance + pendingCollateral);
        log.info("diff:" + diff);

        LegalEntity client = agreement.getClientSignsRelation().getLegalEntity();
        LegalEntity counterpart = agreement.getCounterpartSignsRelation().getLegalEntity();

        if(Math.abs(diff) > agreement.getClientSignsRelation().getMTA())
        {
            //new mc
            LocalDate valuationDate = LocalDate.now();
            LocalDate callDate = valuationDate.plusDays(1);
            Types.MarginType marginType = Types.MarginType.Variation;
            Double callAmount = diff;
            String todayFormatted = valuationDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String mcId = todayFormatted + "-" + agreement.getAgreementId() + "-"+ marginType.name().toString();
            String direction;
            Double deliverAmount;
            Double returnAmount;
            Double excessAmount = diff;
            if(diff <0) {
                direction = "OUT";
                if(balance+pendingCollateral < 0)
                {
                    deliverAmount = excessAmount;
                    returnAmount = 0d;
                }
                else
                if(0 < balance + pendingCollateral  && balance + pendingCollateral < 0-excessAmount)
                {
                    deliverAmount = excessAmount + (balance+pendingCollateral);
                    returnAmount = 0- (balance+pendingCollateral);
                }
                else
                {
                    deliverAmount = 0d;
                    returnAmount = excessAmount;
                }
            }
            else {
                direction = "IN";
                if(balance+pendingCollateral > 0)
                {
                    deliverAmount = excessAmount;
                    returnAmount = 0d;
                }
                else
                if(0 > balance && balance > 0-excessAmount)
                {
                    deliverAmount = excessAmount + (balance+pendingCollateral);
                    returnAmount = 0- (balance+pendingCollateral);
                }
                else
                {
                    deliverAmount = 0d;
                    returnAmount = excessAmount;
                }
            }

            //round amount
            Double rounding = clientSignsRelation.getRounding();
            if(rounding != null && rounding.doubleValue() != 0) {
                if(deliverAmount != null && deliverAmount.doubleValue() != 0)
                    deliverAmount = deliverAmount/Math.abs(deliverAmount)*Math.ceil(Math.abs(deliverAmount) / rounding) * rounding;
                if(returnAmount != null && returnAmount.doubleValue() != 0)
                    returnAmount = returnAmount/Math.abs(returnAmount)*Math.floor(Math.abs(returnAmount) / rounding) * rounding;
            }
            excessAmount = deliverAmount + returnAmount;


            MarginCall marginCall = new MarginCall();
            marginCall.setMarginCallId(mcId);
            marginCall.setExcessAmount(format(diff));
            marginCall.setPendingCollateral(format(pendingCollateral));
            marginCall.setBalanceAmount(format(balance));
            marginCall.setAgreement(agreement);
            marginCall.setReturnAmount(format(returnAmount));
            marginCall.setDeliverAmount(format(deliverAmount));
            marginCall.setValuationDate(valuationDate);
            marginCall.setCallDate(callDate);
            marginCall.setMarginType(marginType);
            marginCall.setCurrency(agreement.getCurrency().getCode());
            marginCall.setDirection(direction);
            Step step = new Step();
            step.setStatus(CallStatus.Expected);
            marginCall.setFirstStep(step);
            marginCall.setLastStep(step);
            marginCall.setMarginAmount(format(excessAmount));
            marginCall.setStatus(CallStatus.Expected.name());
            marginCall.setNotificationTime(callDate.atTime(agreement.getNotificationTime()));
            marginCall.setExposure(format(pv));

            //get ms
            String msId = todayFormatted + "-" + agreement.getAgreementId();
            MarginStatement marginStatement = marginStatementService.findById(msId);

            if(marginStatement == null)
            {
                //create ms
                marginStatement = new MarginStatement();
                marginStatement.setStatementId(msId);
                marginStatement.setDirection(direction);
                marginStatement.setCurrency(agreement.getCurrency());
                marginStatement.setDate(LocalDateTime.now());
                marginStatement.setMarginCalls(new HashSet<MarginCall>(){{
                    add(marginCall);
                }});
                agreement.getMarginStatements().add(marginStatement);

                if(direction.equals("IN"))
                {
                    if(counterpart.getMarginStatements() == null)
                        counterpart.setMarginStatements(new HashSet<>());
                    counterpart.getMarginStatements().add(marginStatement);
                    if(client.getFromMarginStatements() == null)
                        client.setFromMarginStatements(new HashSet<>());
                    client.getFromMarginStatements().add(marginStatement);
                }
                else
                {
                    if(client.getMarginStatements() == null)
                        client.setMarginStatements(new HashSet<>());
                    client.getMarginStatements().add(marginStatement);
                    if(counterpart.getFromMarginStatements() == null)
                        counterpart.setFromMarginStatements(new HashSet<>());
                    counterpart.getFromMarginStatements().add(marginStatement);
                }

                agreementService.createOrUpdate(agreement);
            }
            else
            {
                marginStatement.getMarginCalls().add(marginCall);
            }

            marginStatementService.createOrUpdate(marginStatement);


        }
        else
        {
            return false;
        }

        return true;


    }

    private Double getFXValue(com.opengamma.strata.basics.currency.Currency from, com.opengamma.strata.basics.currency.Currency to, Double value)
    {
        double fromRate = 1;
        if(!from.equals(com.opengamma.strata.basics.currency.Currency.USD))
            fromRate = currencyService.getFXValue(from.getCode());
        double toRate = 1;
        if(!to.equals(com.opengamma.strata.basics.currency.Currency.USD))
            toRate = currencyService.getFXValue(to.getCode());

        return value * toRate / fromRate;
    }

    private Double round(Double deliverAmount, Double returnAmount, Double rounding)
    {
        if(rounding != null && rounding.doubleValue() != 0) {
            deliverAmount = Math.floor(deliverAmount / rounding) * rounding;
            returnAmount = Math.ceil(returnAmount / rounding) * rounding;
        }
        return deliverAmount + returnAmount;
    }



    private double format(double d)
    {

        return Double.parseDouble(df.format(d));
    }
}
