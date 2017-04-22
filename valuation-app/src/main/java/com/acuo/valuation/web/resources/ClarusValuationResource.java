package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus;
import com.acuo.valuation.services.MarginCalcService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.codahale.metrics.annotation.Timed;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/clarus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClarusValuationResource {

    private final MarginCalcService marginCalcService;
    private final TradeService<Trade> tradeService;

    @Inject
    public ClarusValuationResource(MarginCalcService marginCalcService, TradeService<Trade> tradeService) {
        this.marginCalcService = marginCalcService;
        this.tradeService = tradeService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/swapid/{id}/{format}/{type}")
    @Timed
    public MarginResults valuation(@PathParam("id") String id, @PathParam("format") String format, @PathParam("type") String type) {
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();

        Trade trade = tradeService.find(id);
        if(trade != null)
        {
            SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS)trade);
            swapTrades.add(swapTrade);
        }

        return marginCalcService.send(swapTrades, Clarus.DataModel.valueOf(format), Clarus.MarginCallType.valueOf("VM"));

    }
}
