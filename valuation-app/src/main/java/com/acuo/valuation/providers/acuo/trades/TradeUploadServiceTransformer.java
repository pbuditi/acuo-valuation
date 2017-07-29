package com.acuo.valuation.providers.acuo.trades;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.results.TradeValuation;
import com.acuo.common.type.TypedString;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.builders.TradeBuilder;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class TradeUploadServiceTransformer extends TradeUploadServiceAbstract {

    private final TradeService<Trade> tradeService;
    private final Transformer<com.acuo.common.model.trade.Trade> portfolioTransformer;
    private final Transformer<TradeValuation> valuationTransformer;
    private final ResultPersister<PortfolioResults> persister;

    @Inject
    public TradeUploadServiceTransformer(TradingAccountService accountService,
                                         PortfolioService portfolioService,
                                         TradeService<Trade> tradeService,
                                         @Named("portfolio") Transformer<com.acuo.common.model.trade.Trade> portfolioTransformer,
                                         @Named("tradeValuation") Transformer<TradeValuation> valuationTransformer,
                                         ResultPersister<PortfolioResults> persister) {
        super(accountService, portfolioService);
        this.tradeService = tradeService;
        this.portfolioTransformer = portfolioTransformer;
        this.valuationTransformer = valuationTransformer;
        this.persister = persister;
    }

    public List<String> fromExcel(InputStream fis) {
        try {
            List<Trade> trades = buildTrades(fis);
            return trades.stream().map(Trade::getTradeId).map(TypedString::toString).collect(toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Portfolio> fromExcelWithValues(InputStream fis) {
        try {
            List<Trade> trades = buildTrades(fis);
            Set<PortfolioId> portfolios =trades.stream().map(Trade::getPortfolio).map(Portfolio::getPortfolioId).collect(toSet());
            saveValuations(fis);
            return portfolios.stream().map(id -> portfolioService.find(id, 2)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Trade> buildTrades(InputStream fis) throws IOException {
        List<com.acuo.common.model.trade.Trade> trades = portfolioTransformer.deserialise(IOUtils.toByteArray(fis));
        List<Trade> tradeIdList = trades.stream().map(this::buildTradeNew).collect(Collectors.toList());
        tradeService.createOrUpdate(tradeIdList);
        return tradeIdList;
    }

    private void saveValuations(InputStream fis) throws IOException {
        List<TradeValuation> tradeValuations = valuationTransformer.deserialise(IOUtils.toByteArray(fis));
        PortfolioResults results = new PortfolioResults();
        results.setResults(tradeValuations.stream().map(Result::success).collect(Collectors.toList()));
        results.setCurrency(Currency.USD);
        results.setValuationDate(LocalDate.now());
        persister.persist(results);
    }

    private Trade buildTradeNew(com.acuo.common.model.trade.Trade t) {
        Trade trade = TradeBuilder.build(t);
        linkPortfolio(trade, t.getInfo().getPortfolio());
        linkAccount(trade, t.getInfo().getTradingAccountId());
        return trade;
    }
}
