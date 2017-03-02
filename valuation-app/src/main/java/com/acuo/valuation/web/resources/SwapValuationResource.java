package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.SwapService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

    private final PricingService pricingService;
    private final VelocityEngine velocityEngine;
    private final ModelMapper mapper;
    private final SwapService swapService;

    @Inject
    public SwapValuationResource(PricingService pricingService, VelocityEngine velocityEngine, ModelMapper mapper, SwapService swapService) {
        this.velocityEngine = velocityEngine;
        this.pricingService = pricingService;
        this.mapper = mapper;
        this.swapService = swapService;
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
        PricingResults result = pricingService.price(Arrays.asList(swapTrade));
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/swapid/{id}")
    @Timed
    public MarginCallDetail priceBySwapId(@PathParam("id") Long id) throws Exception {
        PricingResults results = swapService.price(ImmutableList.of(id));
        List<MarginCall> marginCalls = swapService.persistMarkitResult(results, false);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/portfolioid/{id}")
    @Timed
    public MarginCallDetail priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        PricingResults results = swapService.pricePortfolio(portfolioId);
        List<MarginCall> marginCalls = swapService.persistMarkitResult(results, false);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/clientid/{id}")
    @Timed
    public PricingResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        PricingResults result = swapService.priceClientTrades(clientId);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/allBilateralIRS")
    @Timed
    public MarginCallDetail priceallBilateralIRS() throws Exception {
        PricingResults results = swapService.valuationAllBilateralIRS();
        List<MarginCall> marginCalls = swapService.persistMarkitResult(results, false);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }
}