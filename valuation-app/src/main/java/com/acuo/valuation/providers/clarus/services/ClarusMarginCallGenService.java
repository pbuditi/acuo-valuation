package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.*;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.valuation.services.ClearedMarginCallGenService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Slf4j
public class ClarusMarginCallGenService implements ClearedMarginCallGenService {

    private final MarginStatementService marginStatementService;
    private final AgreementService agreementService;
    private final CurrencyService currencyService;


    DecimalFormat df = new DecimalFormat("#.##");
    Double pv = null;
    com.opengamma.strata.basics.currency.Currency currencyOfValue = null;

    @Inject
    public ClarusMarginCallGenService(MarginStatementService marginStatementService, AgreementService agreementService, CurrencyService currencyService) {

        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    @Override
    public MarginCall geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation<MarginValuation> valuation) {
        valuation.getValues().stream().filter(value -> value.getSource().equals("Markit")).forEach(value -> {
            pv = value.getPv();
            currencyOfValue = value.getCurrency();
        });


        ClientSignsRelation clientSignsRelation = agreement.getClientSignsRelation();
        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();


        Double balance = clientSignsRelation.getInitialMarginBalance() != null ? clientSignsRelation.getInitialMarginBalance() : 0;
        Double pendingCollateral = clientSignsRelation.getInitialPending() != null ? clientSignsRelation.getInitialPending() : 0;

        if (!currencyOfValue.equals(agreement.getCurrency()))
            pv = getFXValue(currencyOfValue, agreement.getCurrency(), pv);


        Double diff = pv - (balance + pendingCollateral);


        Double exposure;

        if (diff > 0) {
            exposure = pv;
        } else {

            balance = 0 - balance;
            pendingCollateral = 0 - pendingCollateral;
            exposure = 0 - pv;
        }


        double initialBalanceCashToVariation = 1;
        double initialBalanceCash = 1000;
        if (initialBalanceCashToVariation == 1 && diff > 0 && initialBalanceCash > 0) {
            if (initialBalanceCash >= diff) {
                //set some value in client sign,and return
                return null;
            } else {
                //step 5
            }
        }

        //new mc
        LocalDate valuationDate = LocalDate.now();
        LocalDate callDate = valuationDate.plusDays(1);
        Types.MarginType marginType = Types.MarginType.Variation;
        String todayFormatted = valuationDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String mcId = todayFormatted + "-" + agreement.getAgreementId() + "-" + marginType.name().toString();
        String direction;
        Double deliverAmount = 0d;
        Double returnAmount = 0d;
        Double excessAmount = diff;
        Double marginAmount = diff;


        double amount = balance + pendingCollateral;
        if (diff < 0) {
            exposure = 0 - pv;
            direction = "OUT";

        } else {
            exposure = pv;
            direction = "IN";
        }

        if (sign(exposure) == sign(amount) && exposure > 0) {
            deliverAmount = marginAmount;
            returnAmount = 0d;
        } else if (sign(exposure) == sign(amount) && exposure < 0) {
            deliverAmount = 0d;
            returnAmount = marginAmount;
        } else {
            deliverAmount = exposure;
            returnAmount = Math.abs(amount);
        }


        //round amount
//        if(rounding != 0) {
//            if(deliverAmount != null && deliverAmount.doubleValue() != 0)
//                deliverAmount = deliverAmount/Math.abs(deliverAmount)*Math.ceil(Math.abs(deliverAmount) / rounding) * rounding;
//            if(returnAmount != null && returnAmount.doubleValue() != 0)
//                returnAmount = returnAmount/Math.abs(returnAmount)*Math.floor(Math.abs(returnAmount) / rounding) * rounding;
//        }
        marginAmount = deliverAmount + returnAmount;


        MarginCall marginCall = new MarginCall(mcId,
                callDate,
                marginType,
                direction,
                valuationDate,
                agreement.getCurrency().getCode(),
                format(excessAmount),
                format(balance),
                format(deliverAmount),
                format(returnAmount),
                format(pendingCollateral),
                format(exposure),
                0,
                callDate.atTime(agreement.getNotificationTime()),
                format(marginAmount),
                CallStatus.Expected.name());


        Step step = new Step();
        step.setStatus(CallStatus.Expected);
        marginCall.setFirstStep(step);
        marginCall.setLastStep(step);

        //get ms
        String msId = todayFormatted + "-" + agreement.getAgreementId();
        MarginStatement marginStatement = marginStatementService.findById(msId);

        log.info("msid:" + msId);

        if (marginStatement == null) {
            //create ms
            LegalEntity client = agreement.getClientSignsRelation().getLegalEntity();
            LegalEntity counterpart = agreement.getCounterpartSignsRelation().getLegalEntity();
            marginStatement = new MarginStatement();
            marginStatement.setStatementId(msId);
            marginStatement.setDirection(direction);
            marginStatement.setCurrency(agreement.getCurrency());
            marginStatement.setDate(LocalDateTime.now());
            marginStatement.setStatementItems(new HashSet<StatementItem>() {{
                add(marginCall);
            }});

            if (direction.equals("IN")) {
                marginStatement.setDirectedTo(counterpart);
                marginStatement.setSentFrom(client);
            } else {
                marginStatement.setDirectedTo(client);
                marginStatement.setSentFrom(counterpart);
            }

            marginStatement.setAgreement(agreement);

        } else {
            marginStatement.getMarginCalls().add(marginCall);
        }

        marginStatementService.createOrUpdate(marginStatement);


        return marginCall;


    }

    private Double getFXValue(com.opengamma.strata.basics.currency.Currency from, com.opengamma.strata.basics.currency.Currency to, Double value) {
        double fromRate = 1;
        if (!from.equals(com.opengamma.strata.basics.currency.Currency.USD))
            fromRate = currencyService.getFXValue(from.getCode());
        double toRate = 1;
        if (!to.equals(com.opengamma.strata.basics.currency.Currency.USD))
            toRate = currencyService.getFXValue(to.getCode());

        return value * toRate / fromRate;
    }

    private Double round(Double deliverAmount, Double returnAmount, Double rounding) {
        if (rounding != null && rounding.doubleValue() != 0) {
            deliverAmount = Math.floor(deliverAmount / rounding) * rounding;
            returnAmount = Math.ceil(returnAmount / rounding) * rounding;
        }
        return deliverAmount + returnAmount;
    }


    private double format(double d) {

        return Double.parseDouble(df.format(d));
    }

    private int sign(double d) {
        if (d > 0)
            return 1;
        if (d < 0)
            return -1;
        else
            return 0;
    }
}
