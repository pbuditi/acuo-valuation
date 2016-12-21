package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.SwapService;
import com.codahale.metrics.annotation.Timed;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.Arrays;

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
    @Path("/price/swapid/{id}")
    @Timed
    public PricingResults priceBySwapId(@PathParam("id") String id) throws Exception {
        PricingResults result = swapService.price(id);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/price/clientid/{id}")
    @Timed
    public PricingResults getPv(@PathParam("id") String id) throws Exception {
        PricingResults result = swapService.priceClientTrades(id);
        return result;
    }
}