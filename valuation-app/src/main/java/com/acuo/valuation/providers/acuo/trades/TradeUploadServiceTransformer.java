package com.acuo.valuation.providers.acuo.trades;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.type.TypedString;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.utils.TradeBuilder;
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
    private final Transformer<SwapTrade> transformer;

    @Inject
    public TradeUploadServiceTransformer(TradingAccountService accountService,
                                         PortfolioService portfolioService,
                                         TradeService<Trade> tradeService,
                                         @Named("portfolio") Transformer<SwapTrade> transformer) {
        super(accountService, portfolioService);
        this.tradeService = tradeService;
        this.transformer = transformer;
    }

    public List<String> fromExcel(InputStream fis) {
        List<Trade> tradeIdList = new ArrayList<>();
        List<SwapTrade> swapTrades;

        try {
            swapTrades = transformer.deserialise(IOUtils.toByteArray(fis));
            tradeIdList = swapTrades.stream().map(this::buildTradeNew).collect(Collectors.toList());
            tradeService.createOrUpdate(tradeIdList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return tradeIdList.stream().map(Trade::getTradeId).map(TypedString::toString).collect(toList());
    }

    private Trade buildTradeNew(SwapTrade swapTrade) {
        TradeBuilder tradeBuilder = new TradeBuilder();
        Trade trade = tradeBuilder.build(swapTrade);
        linkPortfolio(trade, swapTrade.getInfo().getPortfolio());
        linkAccount(trade, swapTrade.getInfo().getTradingAccountId());
        return trade;
    }
}
