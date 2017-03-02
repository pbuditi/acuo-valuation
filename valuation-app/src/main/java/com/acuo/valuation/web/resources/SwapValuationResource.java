package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.MarkitValautionsProcessor;
import com.acuo.valuation.services.PricingService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

    private final PricingService pricingService;
    private final MarkitValautionsProcessor resultProcessor;
    private final VelocityEngine velocityEngine;
    private final ModelMapper mapper;

    @Inject
    public SwapValuationResource(PricingService pricingService,
                                 MarkitValautionsProcessor resultProcessor,
                                 VelocityEngine velocityEngine,
                                 ModelMapper mapper) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
        this.velocityEngine = velocityEngine;
        this.mapper = mapper;
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
    public PricingResults price(SwapTrade swapTrade) throws Exception {
        PricingResults result = pricingService.priceSwapTrades(Arrays.asList(swapTrade));
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/swapid/{id}")
    @Timed
    public MarginCallDetail priceBySwapId(@PathParam("id") String id) throws Exception {
        PricingResults results = pricingService.priceTradeIds(ImmutableList.of(id));
        List<MarginCall> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/portfolioid/{id}")
    @Timed
    public MarginCallDetail priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        PricingResults results = pricingService.priceTradesUnder(portfolioId);
        List<MarginCall> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/clientid/{id}")
    @Timed
    public PricingResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        PricingResults result = pricingService.priceTradesOf(clientId);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/allBilateralIRS")
    @Timed
    public MarginCallDetail priceallBilateralIRS() throws Exception {
        PricingResults results = pricingService.priceTradesOfType("Bilateral");
        List<MarginCall> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }
}