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
            return pricingService.price(swapTrades);
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

                            valuationService.createOrUpdate(valuation);

                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    //new valutaion

                    Valuation valuation = new Valuation();

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
                    valuationService.createOrUpdate(valuation);

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

        if(Math.abs(pv) > clientSignsRelation.getThreshold())
            return false;

        Double diff = pv - (balance + pendingCollateral);

        if(Math.abs(diff) > agreement.getClientSignsRelation().getMTA())
        {
            //new mc
            LocalDate valuationDate = LocalDate.now();
            LocalDate callDate = valuationDate.plusDays(1);
            Types.CallType marginType = Types.CallType.Variation;
            Double callAmount = diff;
            String todayFormatted = valuationDate.format(DateTimeFormatter.ofPattern("yyyy/mm/dd"));
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
            excessAmount = round(deliverAmount, returnAmount, clientSignsRelation.getRounding());


            MarginCall marginCall = new MarginCall();
            marginCall.setMarginCallId(mcId);
            marginCall.setExcessAmount(excessAmount);
            marginCall.setPendingCollateral(pendingCollateral);
            marginCall.setBalanceAmount(balance);
            marginCall.setAgreement(agreement);

            marginCall.setCallDate(callDate);
            marginCall.setCallType(marginType.name());
            marginCall.setCurrency(agreement.getCurrency().getCode());
            marginCall.setDirection(direction);
            Step step = new Step();
            step.setStatus(CallStatus.Expected);
            marginCall.setFirstStep(step);
            marginCall.setLastStep(step);

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
        return deliverAmount + returnAmount;
    }

}