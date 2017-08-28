package com.acuo.valuation.providers.acuo.portfolios;

import com.acuo.common.model.ids.PortfolioId;
import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.jackson.MarginCallResult;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import com.acuo.valuation.services.PortfolioManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.acuo.common.model.margin.Types.CallType.Initial;
import static com.acuo.common.model.margin.Types.CallType.Variation;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class PortfolioManagerImpl implements PortfolioManager {

    private final TradeService<Trade> tradeService;
    private final TradePricingProcessor tradePricingProcessor;
    private final PortfolioService portfolioService;
    private final ValuationService valuationService;

    @Inject
    public PortfolioManagerImpl(TradeService<Trade> tradeService,
                                TradePricingProcessor tradePricingProcessor,
                                PortfolioService portfolioService,
                                ValuationService valuationService) {
        this.tradeService = tradeService;
        this.tradePricingProcessor = tradePricingProcessor;
        this.portfolioService = portfolioService;
        this.valuationService = valuationService;
    }

    public List<Portfolio> valueMissing(List<String> portfolioIds, LocalDate valuationDate) {
        Set<PortfolioId> ids = portfolioIds.stream().map(PortfolioId::fromString).collect(toSet());
        PortfolioId[] ts = ids.toArray(new PortfolioId[ids.size()]);
        Iterable<Trade> tradeList = tradeService.findByPortfolioId(ts);
        if (tradeList == null || Iterables.size(tradeList) == 0)
            return null;
        List<Trade> filtered = StreamSupport.stream(tradeList.spliterator(), false)
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
        return StreamSupport.stream(portfolioService.portfolios(ts).spliterator(), false)
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
        return valuationService.isPortfolioValuated(portfolio.getPortfolioId(), callType, valuationDate);
    }

    private boolean isTradeValuated(Trade trade, LocalDate valuationDate) {
        return valuationService.isTradeValuated(trade.getTradeId(), valuationDate);
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
