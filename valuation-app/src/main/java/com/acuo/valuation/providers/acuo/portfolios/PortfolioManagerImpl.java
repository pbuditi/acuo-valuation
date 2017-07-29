package com.acuo.valuation.providers.acuo.portfolios;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.jackson.MarginCallResult;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import com.acuo.valuation.services.PortfolioManager;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.acuo.common.model.margin.Types.CallType.Initial;
import static com.acuo.common.model.margin.Types.CallType.Variation;
import static java.util.stream.Collectors.toList;

public class PortfolioManagerImpl implements PortfolioManager {

    private final TradeService<Trade> tradeService;
    private final TradePricingProcessor tradePricingProcessor;
    private final PortfolioService portfolioService;
    private final ValuationService valuationService;

    @Inject
    public PortfolioManagerImpl(TradeService<Trade> tradeService,
                                TradePricingProcessor tradePricingProcessor,
                                PortfolioService portfolioService,
                                ValuationService valuationService){
        this.tradeService = tradeService;
        this.tradePricingProcessor = tradePricingProcessor;
        this.portfolioService = portfolioService;
        this.valuationService = valuationService;
    }

    public List<Portfolio> valueMissing(List<String> portfolioIds, LocalDate valuationDate) {
        List<Trade> tradeList = portfolioIds.stream()
                .map(PortfolioId::fromString)
                .map(tradeService::findByPortfolioId)
                .flatMap(trades -> StreamSupport.stream(trades.spliterator(), false))
                .collect(toList());
        if (tradeList == null || tradeList.size() == 0)
            return null;
        List<Trade> filtered = tradeList.stream()
                .map(trade -> tradeService.find(trade.getTradeId(), 2))
                .filter(Objects::nonNull)
                .filter(trade -> {
                    Portfolio portfolio = trade.getPortfolio();
                    Agreement agreement = portfolio.getAgreement();
                    String type = agreement.getType();
                    if ("bilateral".equals(type) || "legacy".equals(type)) {
                        return !isTradeValuated(trade, valuationDate);
                    } else {
                        return !isPortfolioValuated(portfolio, valuationDate, Variation) ||
                                !isPortfolioValuated(portfolio, valuationDate, Initial);
                    }
                })
                .collect(toList());
        tradePricingProcessor.process(filtered);
       return portfolioIds.stream()
               .map(PortfolioId::fromString)
               .map(id -> portfolioService.find(id, 2))
               .collect(toList());
    }

    public MarginCallResponse split(List<Portfolio> portfolios, LocalDate valuationDate) {
        final List<MarginCallResult> details = portfolios.stream()
                .map(portfolio -> {
                    Agreement agreement = portfolio.getAgreement();
                    String type = agreement.getType();
                    if ("bilateral".equals(type) || "legacy".equals(type)) {
                        MarginCallResult callResult = bilateral(portfolio, valuationDate, Variation);
                        return ImmutableList.of(callResult);
                    } else {
                        MarginCallResult vm = cleared(portfolio, valuationDate, Variation);
                        MarginCallResult im = cleared(portfolio, valuationDate, Initial);
                        return ImmutableList.of(vm, im);
                    }
                })
                .flatMap(Collection::stream)
                .collect(toList());
        return MarginCallResponse.ofPortfolio(details);
    }

    private boolean isPortfolioValuated(Portfolio portfolio, LocalDate valuationDate, Types.CallType callType) {
        MarginValuation marginValuation = valuationService.getMarginValuationFor(portfolio.getPortfolioId(), callType);
        if (marginValuation != null && marginValuation.getValues() != null) {
            for (MarginValue marginValue : marginValuation.getValues()) {
                if (valuationDate.equals(marginValue.getValuationDate())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTradeValuated(Trade trade, LocalDate valuationDate) {
        TradeValuation tradeValuation = valuationService.getTradeValuationFor(trade.getTradeId());
        if (tradeValuation != null && tradeValuation.getValues() != null) {
            for (TradeValue tradeValue : tradeValuation.getValues()) {
                if (valuationDate.equals(tradeValue.getValuationDate())) {
                    return true;
                }
            }
        }
        return false;
    }

    private MarginCallResult bilateral(Portfolio portfolio, LocalDate valuationDate, Types.CallType callType) {
        PortfolioId portfolioId = portfolio.getPortfolioId();
        Long totalTradeCount = valuationService.tradeCount(portfolioId);
        Long valuatedTradeCount = valuationService.tradeValuedCount(portfolioId, valuationDate);
        Double totalPV = 0.0d;
        MarginValuation marginValuation = valuationService.getMarginValuationFor(portfolioId, callType);
        if (marginValuation != null && marginValuation.getValues() != null) {
            for (MarginValue marginValue : marginValuation.getValues()) {
                if (marginValue.getValuationDate().equals(valuationDate)) {
                    totalPV = marginValue.getAmount();
                    break;
                }
            }
        }
        return MarginCallResult.of(portfolio, valuationDate, callType.name(), "Markit", totalTradeCount, valuatedTradeCount, totalPV);
    }

    private MarginCallResult cleared(Portfolio portfolio, LocalDate valuationDate, Types.CallType callType) {
        PortfolioId portfolioId = portfolio.getPortfolioId();
        Long totalTradeCount = valuationService.tradeCount(portfolioId);
        Long valuatedTradeCount = 0L;
        Double totalPV = 0.0d;
        MarginValuation marginValuation = valuationService.getMarginValuationFor(portfolioId, callType);
        if (marginValuation != null && marginValuation.getValues() != null) {
            for (MarginValue marginValue : marginValuation.getValues()) {
                if (marginValue.getValuationDate().equals(valuationDate)) {
                    valuatedTradeCount = totalTradeCount;
                    totalPV = marginValue.getAmount();
                    break;
                }
            }
        }
        return MarginCallResult.of(portfolio, valuationDate, callType.name(), "Clarus", totalTradeCount, valuatedTradeCount, totalPV);
    }
}
