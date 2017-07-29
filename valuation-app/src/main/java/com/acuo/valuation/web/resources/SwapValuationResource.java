package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.ClientId;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.jackson.PortfolioIds;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.TradeProcessor;
import com.acuo.valuation.services.PricingService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

    private final PricingService pricingService;
    private final TradeService<Trade> tradeService;
    private final TradeProcessor tradeProcessor;
    private final VelocityEngine velocityEngine;


    @Inject
    public SwapValuationResource(PricingService pricingService,
                                 TradeService<Trade> tradeService,
                                 TradeProcessor tradeProcessor,
                                 VelocityEngine velocityEngine) {
        this.pricingService = pricingService;
        this.tradeService = tradeService;
        this.tradeProcessor = tradeProcessor;
        this.velocityEngine = velocityEngine;

    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/")
    public String hello() {
        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate("velocity/swaps.vm", "UTF-8", new VelocityContext(), writer);
        return writer.toString();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/value")
    @Timed
    public MarkitResults price(SwapTrade swapTrade) throws Exception {
        log.info("Pricing the trade {}", swapTrade);
        return pricingService.priceSwapTrades(singletonList(swapTrade));
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/trade/{id}")
    @Timed
    public MarginCallResponse priceBySwapId(@PathParam("id") String id) throws Exception {
        log.info("Pricing the trade {}", id);
        List<Trade> trades = ImmutableList.of(id).stream()
                .map(tradeId -> tradeService.find(TradeId.fromString(tradeId)))
                .collect(toList());
        Collection<MarginCall> marginCalls = tradeProcessor.process(trades);
        return MarginCallResponse.of(marginCalls);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/portfolio/id/{id}")
    @Timed
    public MarginCallResponse priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        log.info("Pricing all trades under the portfolio {}", portfolioId);
        Iterable<Trade> iterable = tradeService.findByPortfolioId(portfolioId);
        List<Trade> trades = StreamSupport.stream(iterable.spliterator(), false).collect(toList());
        Collection<MarginCall> marginCalls = tradeProcessor.process(trades);
        return MarginCallResponse.of(marginCalls);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/portfolios")
    @Timed
    public MarginCallResponse priceByPortfolios(PortfolioIds portfolioIds) throws Exception {
        log.info("price all trades of portfolios {}", portfolioIds);
        List<PortfolioId> ids = portfolioIds.getIds().stream()
                .map(PortfolioId::fromString)
                .collect(toList());
        Iterable<Trade> iterable = tradeService.findByPortfolioId(ids.toArray(new PortfolioId[ids.size()]));
        List<Trade> trades = StreamSupport.stream(iterable.spliterator(), false).collect(toList());
        Collection<MarginCall> marginCalls = tradeProcessor.process(trades);
        return MarginCallResponse.of(marginCalls);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/clientid/{id}")
    @Timed
    public MarkitResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        log.info("Pricing all trades of client {}", clientId);
        return pricingService.priceTradesOf(clientId);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/allBilateralIRS")
    @Timed
    public MarginCallResponse priceAllBilateralIRS() throws Exception {
        log.info("Pricing all bilateral trades");
        Iterable<IRS> allIRS = tradeService.findAllIRS();
        Collection<MarginCall> marginCalls = tradeProcessor.process(allIRS);
        return MarginCallResponse.of(marginCalls);
    }
}