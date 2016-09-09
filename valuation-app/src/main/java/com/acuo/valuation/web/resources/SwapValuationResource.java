package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.requests.dto.SwapDTO;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.dto.SwapResultDTO;
import com.acuo.valuation.services.PricingService;
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

    @Inject
    public SwapValuationResource(PricingService pricingService, VelocityEngine velocityEngine, ModelMapper mapper) {
        this.velocityEngine = velocityEngine;
        this.pricingService = pricingService;
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
        PricingResults result = pricingService.price(Arrays.asList(swapTrade));
        return result;
    }
}