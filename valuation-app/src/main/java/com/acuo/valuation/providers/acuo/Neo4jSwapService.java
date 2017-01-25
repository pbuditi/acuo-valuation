package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.margin.Types;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.*;
import com.acuo.persist.services.*;
import com.acuo.valuation.protocol.results.*;
import com.acuo.valuation.protocol.results.Value;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.SwapService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class Neo4jSwapService implements SwapService {

    private final PricingService pricingService;
    //private final Neo4jPersistService sessionProvider;
    private final TradeService<Trade> tradeService;
    private final ValuationService valuationService;
    private final PortfolioService portfolioService;
    private final ValueService valueService;
    private final MarginStatementService marginStatementService;
    private final AgreementService agreementService;

    Double pv = null;
    Currency currencyOfValue = null;

    @Inject
    public Neo4jSwapService(PricingService pricingService, /*Neo4jPersistService sessionProvider,*/ TradeService<Trade> tradeService, ValuationService valuationService, PortfolioService portfolioService, ValueService valueService,
                            MarginStatementService marginStatementService, AgreementService agreementService) {
        this.pricingService = pricingService;
        //this.sessionProvider = sessionProvider;
        this.tradeService = tradeService;
        this.valuationService = valuationService;
        this.portfolioService = portfolioService;
        this.valueService = valueService;
        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
    }

    @Override
    public PricingResults price(String swapId) {
        try {
            List<SwapTrade> swapTrades = getSwapTrades(swapId);
            PricingResults results = pricingService.price(swapTrades);
            persistMarkitResult(results);
            return results;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public PricingResults priceClientTrades(String clientId) {
        Iterable<Trade> trades = tradeService.findBilateralTradesByClientId(clientId);
        List<SwapTrade> swapTrades = StreamSupport.stream(trades.spliterator(), false)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> SwapTradeBuilder.buildTrade((IRS) trade))
                .collect(Collectors.toList());
        return pricingService.price(swapTrades);
    }

    @Override
    public boolean persistMarkitResult(PricingResults pricingResults) {
        if (pricingResults == null) {
            log.warn("received a null pricing results to persistMarkitResult");
            return false;
        }

        LocalDate date = pricingResults.getDate();

        Currency currency = pricingResults.getCurrency();

        log.debug("persistMarkitResult start :" + pricingResults.getDate());

        ImmutableList<com.opengamma.strata.collect.result.Result<MarkitValuation>> results = pricingResults.getResults();
        for (com.opengamma.strata.collect.result.Result<MarkitValuation> result : results) {
            log.debug(result.toString());
            for (Value value : result.getValue().getValues()) {
                String tradeId = value.getTradeId();
                Double pv = value.getPv();

                log.debug("tradeId:" + tradeId);

                Trade trade = tradeService.findById(Long.valueOf(tradeId));

                log.debug(trade.toString());

                Set<Valuation> valuations = trade.getValuations();
                boolean found = false;
                if (valuations != null) {
                    for (Valuation valuation : valuations) {
                        log.debug("date in valuation : " + valuation.getDate());
                        if (valuation.getDate().equals(date)) {
                            log.debug("existing valuation");
                            //existing date, add or replace the value
                            Set<com.acuo.persist.entity.Value> existedValues = valuation.getValues();
                            for (com.acuo.persist.entity.Value existedValue : existedValues) {
                                if (existedValue.getCurrency().equals(currency) && existedValue.getSource().equalsIgnoreCase("Markit"))
                                    valueService.delete(existedValue.getId());
                            }

                            com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                            newValue.setSource("Markit");
                            newValue.setCurrency(currency);
                            newValue.setPv(value.getPv());

                            valuation.getValues().add(newValue);

//                            if(trade.getPortfolio().getValuations() != null)
//                                trade.getPortfolio().getValuations().add(valuation);
//                            else
//                                trade.getPortfolio().setValuations(new HashSet<Valuation>(){{add(valuation);}});
//
//                            portfolioService.createOrUpdate(trade.getPortfolio());



                            valuationService.createOrUpdate(valuation);
                            addsumValuationOfPortfolio(trade.getPortfolio(), date, currency, "Markit", value.getPv());
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    //new valutaion

                    Valuation valuation = new Valuation();

                    valuation.setValuationId(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "-" + trade.getTradeId());

                    valuation.setDate(date);

                    log.debug("new valuation:" + valuation.getDate());

                    com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                    newValue.setSource("Markit");
                    newValue.setCurrency(currency);
                    newValue.setPv(value.getPv());

                    Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();

                    values.add(newValue);

                    valuation.setValues(values);

                    if(trade.getValuations() != null)
                        trade.getValuations().add(valuation);
                    else
                    {
                        Set<Valuation> valuationSet = new HashSet<Valuation>();
                        trade.setValuations(valuationSet);

                    }

//                    if(trade.getPortfolio().getValuations() != null)
//                        trade.getPortfolio().getValuations().add(valuation);
//                    else
//                        trade.getPortfolio().setValuations(new HashSet<Valuation>(){{add(valuation);}});
//
//                    portfolioService.createOrUpdate(trade.getPortfolio());

                    valuationService.createOrUpdate(valuation);
                    tradeService.createOrUpdate(trade);
                    addsumValuationOfPortfolio(trade.getPortfolio(), date, currency, "Markit", value.getPv());
                }
            }
        }
        return true;
    }

    private List<SwapTrade> getSwapTrades(String swapId) {
        Trade trade = tradeService.findById(Long.valueOf(swapId));
        SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS) trade);
        log.debug("swapTrade:" + swapTrade);
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        swapTrades.add(swapTrade);
        return swapTrades;
    }

    public static LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public boolean persistClarusResult(MarginResults marginResults)
    {


        String portfolioId = marginResults.getPortfolioId();

        Portfolio portfolio = portfolioService.findById(portfolioId);

        if(portfolio == null)
            return false;

        //parse the result
        String currency = marginResults.getCurrency();
        Iterator<Result<MarginValuation>> resultIterator = marginResults.getResults().iterator();
        Valuation newValuation = new Valuation();
        newValuation.setDate(marginResults.getValuationDate());
        com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();
        while(resultIterator.hasNext())
        {
            Result<MarginValuation> result = resultIterator.next();
            MarginValuation marginValuation= result.getValue();
            if(marginValuation.getName().equals(currency))
            {
                newValue.setPv(marginValuation.getMargin());
                newValue.setSource("Clarus");
                newValue.setCurrency(Currency.of(currency));
            }
        }


        log.debug(portfolio.toString());

        Set<Valuation> valuations = portfolio.getValuations();
        if(valuations == null)
        {
            valuations = new HashSet<Valuation>();


        }

        for(Valuation valuation : valuations)
        {
            valuation = valuationService.find(valuation.getId());
            if(valuation.getDate().equals(marginResults.getValuationDate()))
            {
                Set<com.acuo.persist.entity.Value> values = valuation.getValues();
                if(values == null)
                {
                    values = new HashSet<com.acuo.persist.entity.Value>();
                }
                for(com.acuo.persist.entity.Value value : values)
                {
                    if(value.getCurrency().equals(currency) && value.getSource().equals("Clarus"))
                    {
                        //replace this value
                        value.setPv(newValue.getPv());
                        valueService.createOrUpdate(value);
                        return true;

                    }
                }

                values.add(newValue);
                valuation.setValues(values);
                valuationService.createOrUpdate(valuation);
                return true;
            }
        }




        valuations.add(newValuation);
        Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();
        values.add(newValue);
        newValuation.setValues(values);
        portfolio.setValuations(valuations);
        valuationService.createOrUpdate(newValuation);
        portfolioService.createOrUpdate(portfolio);



        return true;
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
            marginCall.setExcessAmount(diff);
            marginCall.setPendingCollateral(pendingCollateral);
            marginCall.setBalanceAmount(balance);
            marginCall.setAgreement(agreement);
            marginCall.setReturnAmount(returnAmount);
            marginCall.setDeliverAmount(deliverAmount);
            marginCall.setValuationDate(valuationDate);
            marginCall.setCallDate(callDate);
            marginCall.setMarginType(marginType);
            marginCall.setCurrency(agreement.getCurrency().getCode());
            marginCall.setDirection(direction);
            Step step = new Step();
            step.setStatus(CallStatus.Expected);
            marginCall.setFirstStep(step);
            marginCall.setLastStep(step);
            marginCall.setMarginAmount(excessAmount);
            marginCall.setStatus(CallStatus.Expected.name());
            marginCall.setNotificationTime(callDate.atTime(agreement.getNotificationTime()));
            marginCall.setExposure(pv);

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

    private Double getFXValue(Currency from, Currency to, Double value)
    {
        return value;
    }

    private Double round(Double deliverAmount, Double returnAmount, Double rounding)
    {
        if(rounding != null && rounding.doubleValue() != 0) {
            deliverAmount = Math.floor(deliverAmount / rounding) * rounding;
            returnAmount = Math.ceil(returnAmount / rounding) * rounding;
        }
        return deliverAmount + returnAmount;
    }

    private void addsumValuationOfPortfolio(Portfolio portfolio, LocalDate date, Currency currency, String source, Double pv)
    {

        portfolio = portfolioService.findById(portfolio.getPortfolioId(), 2);

        Valuation theValuation = null;
        com.acuo.persist.entity.Value theValue = null;



        if(portfolio.getValuations() != null)
        {
            for(Valuation valuation : portfolio.getValuations())
            {
                if(valuation.getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))))
                {
                    theValuation = valuation;
                    if(valuation.getValues() != null)
                        for(com.acuo.persist.entity.Value value : valuation.getValues())
                        {
                            if(value.getCurrency().equals(currency) && value.getSource().equals(source))
                                theValue = value;
                        }
                }
            }
        }


        if(theValuation == null)
        {
            theValuation = new Valuation();
            theValuation.setValuationId(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "-" + portfolio.getPortfolioId());
            theValuation.setDate(date);
            Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();
            theValue = new com.acuo.persist.entity.Value();
            theValue.setPv(pv);
            theValue.setCurrency(currency);
            theValue.setSource(source);
            values.add(theValue);
            theValuation.setValues(values);

            if (portfolio.getValuations() == null)
                portfolio.setValuations(new HashSet<Valuation>());

            portfolio.getValuations().add(theValuation);
            portfolioService.createOrUpdate(portfolio);
        }
        else
        {
            if(theValue == null)
            {
                theValue = new com.acuo.persist.entity.Value();
                theValue.setPv(pv);
                theValue.setCurrency(currency);
                theValue.setSource(source);

                if(theValuation.getValues() == null)
                    theValuation.setValues(new HashSet<com.acuo.persist.entity.Value>());

                theValuation.getValues().add(theValue);
                valuationService.createOrUpdate(theValuation);
            }
            else
            {
                theValue.setPv(theValue.getPv() + pv);
                valueService.createOrUpdate(theValue);
            }
        }





    }

}