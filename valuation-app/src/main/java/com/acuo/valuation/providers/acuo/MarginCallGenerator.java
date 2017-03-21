package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.*;
import com.acuo.persist.services.*;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Slf4j
public abstract class MarginCallGenerator {

    protected static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    protected final ValuationService valuationService;
    protected final PortfolioService portfolioService;
    protected final MarginStatementService marginStatementService;
    protected final AgreementService agreementService;
    protected Double pv = null;

    private final CurrencyService currencyService;
    private DecimalFormat df = new DecimalFormat("#.##");
    protected com.opengamma.strata.basics.currency.Currency currencyOfValue = null;

    public MarginCallGenerator(ValuationService valuationService,
                                       PortfolioService portfolioService,
                                       MarginStatementService marginStatementService,
                                       AgreementService agreementService,
                                       CurrencyService currencyService) {
        this.valuationService = valuationService;
        this.portfolioService = portfolioService;
        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    protected MarginCall generateMarginCall(Agreement agreement, Valuation valuation, CallStatus callStatus) {
        valuation.getValues().stream().filter(value -> value.getSource().equals("Markit")).forEach(value -> {
            pv = value.getPv();
            currencyOfValue = value.getCurrency();
        });
        //reload the agreement object due to the depth fetch
        agreement = agreementService.findById(agreement.getAgreementId(), 2);
        ClientSignsRelation clientSignsRelation = agreement.getClientSignsRelation();
        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();

        Double balance = clientSignsRelation.getVariationMarginBalance() != null ? clientSignsRelation.getVariationMarginBalance() : 0;
        Double pendingCollateral = clientSignsRelation.getVariationPending() != null ? clientSignsRelation.getVariationPending() : 0;

        if (!currencyOfValue.equals(agreement.getCurrency()))
            pv = getFXValue(currencyOfValue, agreement.getCurrency(), pv);

        if (clientSignsRelation.getThreshold() != null && Math.abs(pv) <= clientSignsRelation.getThreshold())
            return null;

        Double diff = pv - (balance + pendingCollateral);


        double MTA;
        double rounding;


        if (diff > 0) {
            MTA = clientSignsRelation.getMTA() != null ? clientSignsRelation.getMTA() : 0;
            rounding = clientSignsRelation.getRounding() != null ? clientSignsRelation.getRounding() : 0;
            balance = clientSignsRelation.getVariationMarginBalance() != null ? clientSignsRelation.getVariationMarginBalance() : 0;
            pendingCollateral = clientSignsRelation.getVariationPending() != null ? clientSignsRelation.getVariationPending() : 0;
        } else {
            MTA = counterpartSignsRelation.getMTA() != null ? counterpartSignsRelation.getMTA() : 0;
            rounding = counterpartSignsRelation.getRounding() != null ? counterpartSignsRelation.getRounding() : 0;
            balance = counterpartSignsRelation.getVariationMarginBalance() != null ? counterpartSignsRelation.getVariationMarginBalance() : 0;
            pendingCollateral = counterpartSignsRelation.getVariationPending() != null ? counterpartSignsRelation.getVariationPending() : 0;
        }


        //if (Math.abs(diff) <= MTA)
        //    return null;
//
//
        //new mc
        LocalDate valuationDate = LocalDate.now();
        LocalDate callDate = valuationDate.plusDays(1);
        Types.MarginType marginType = Types.MarginType.Variation;
        String todayFormatted = valuationDate.format(dateTimeFormatter);
        String mcId = todayFormatted + "-" + agreement.getAgreementId() + "-" + marginType.name().toString();
        String direction;
        Double deliverAmount = 0d;
        Double returnAmount = 0d;
        Double excessAmount = diff;
        Double marginAmount = diff;
        Double exposure;

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
        if (rounding != 0) {
            if (deliverAmount != null && deliverAmount.doubleValue() != 0)
                deliverAmount = deliverAmount / Math.abs(deliverAmount) * Math.ceil(Math.abs(deliverAmount) / rounding) * rounding;
            if (returnAmount != null && returnAmount.doubleValue() != 0)
                returnAmount = returnAmount / Math.abs(returnAmount) * Math.floor(Math.abs(returnAmount) / rounding) * rounding;
        }
        marginAmount = deliverAmount + returnAmount;


        MarginCall marginCall = new InitialMargin(mcId,
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
                callStatus.name());

        Step step = new Step();
        step.setStatus(callStatus);
        marginCall.setFirstStep(step);
        marginCall.setLastStep(step);

        //get ms
        String msId = todayFormatted + "-" + agreement.getAgreementId();
        MarginStatement marginStatement = marginStatementService.findById(msId);

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
            marginStatementService.createOrUpdate(marginStatement);
        }

        marginCall.setMarginStatement(marginStatement);
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

    protected double format(double d) {
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
