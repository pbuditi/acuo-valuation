package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeCacheService;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
@Path("/calls")
public class MarginCallResource {

    private final static Map<String, AsyncResponse> waiters = new ConcurrentHashMap<>();

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
        List<String> trades = cacheService.remove(tnxId);
        PricingResults results = pricingService.priceTradeIds(trades);
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        return Response.status(OK).entity(MarginCallDetail.of(marginCalls)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @Path("/async/generate/{tnxId}")
    public void asyncGenerate(@Suspended AsyncResponse asyncResp, @PathParam("tnxId") String tnxId) {
        log.info("Async generating margin calls for transaction {}", tnxId);
        if (cacheService.contains(tnxId)) {
            waiters.put(tnxId, asyncResp);
            CompletableFuture.supplyAsync(() -> {
                List<String> trades = cacheService.remove(tnxId);
                PricingResults results = pricingService.priceTradeIds(trades);
                return resultProcessor.process(results);
            })
                    .thenApply((result) -> {
                        final AsyncResponse asyncResponse = waiters.get(tnxId);
                        return asyncResponse.resume(Response.status(OK).entity(MarginCallDetail.of(result)).build());
                    })
                    .exceptionally(exp -> {
                                log.error("exception occured in async generate call ", exp);
                                final AsyncResponse asyncResponse = waiters.get(tnxId);
                                return asyncResponse.resume(exp);
                            }
                    );
        } else {
            asyncResp.resume(new NotFoundException("transaction [" + tnxId + "] not found"));
        }
    }
}