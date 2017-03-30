package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.MarginCall;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeCacheService;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
@Path("/calls")
public class MarginCallResource {

    private final PricingService pricingService;
    private final MarkitValuationProcessor resultProcessor;
    private final TradeCacheService cacheService;

    @Inject
    public MarginCallResource(PricingService pricingService,
                              MarkitValuationProcessor resultProcessor,
                              TradeCacheService cacheService) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
        this.cacheService = cacheService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @Path("/generate/{tnxId}")
    public Response generate(@PathParam("tnxId") String tnxId) {
        log.info("Generating margin calls for transaction {}", tnxId);
        List<String> trades = cacheService.get(tnxId);
        PricingResults results = pricingService.priceTradeIds(trades);
        List<MarginCall> marginCalls = resultProcessor.process(results);
        return Response.status(OK).entity(MarginCallDetail.of(marginCalls)).build();
    }
}