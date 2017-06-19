package com.acuo.valuation.providers.acuo.trades;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.type.TypedString;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.builders.TradeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class TradeUploadServiceTransformer extends TradeUploadServiceAbstract {

    private final TradeService<Trade> tradeService;
    private final Transformer<com.acuo.common.model.trade.Trade> transformer;

    @Inject
    public TradeUploadServiceTransformer(TradingAccountService accountService,
                                         PortfolioService portfolioService,
                                         TradeService<Trade> tradeService,
                                         @Named("portfolio") Transformer<com.acuo.common.model.trade.Trade> transformer) {
        super(accountService, portfolioService);
        this.tradeService = tradeService;
        this.transformer = transformer;
    }

    public List<String> fromExcel(InputStream fis) {
        List<Trade> tradeIdList = new ArrayList<>();
        List<com.acuo.common.model.trade.Trade> swapTrades;

        try {
            swapTrades = transformer.deserialise(IOUtils.toByteArray(fis));
            tradeIdList = swapTrades.stream().map(this::buildTradeNew).collect(Collectors.toList());
            tradeService.createOrUpdate(tradeIdList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return tradeIdList.stream().map(Trade::getTradeId).map(TypedString::toString).collect(toList());
    }

    private Trade buildTradeNew(com.acuo.common.model.trade.Trade t) {
        Trade trade = TradeBuilder.build(t);
        linkPortfolio(trade, t.getInfo().getPortfolio());
        linkAccount(trade, t.getInfo().getTradingAccountId());
        return trade;
    }
}
