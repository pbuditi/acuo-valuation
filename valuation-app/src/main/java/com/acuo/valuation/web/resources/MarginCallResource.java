package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
@Path("/calls")
public class MarginCallResource {

    private final static Map<String, AsyncResponse> waiters = new ConcurrentHashMap<>();

    private final TradeCacheService cacheService;
    private final TradePricingProcessor tradePricingProcessor;
    private final TradeService<Trade> tradeService;

    @Inject
    public MarginCallResource(TradeCacheService cacheService,
                              TradePricingProcessor tradePricingProcessor,
                              TradeService<Trade> tradeService) {
        this.cacheService = cacheService;
        this.tradePricingProcessor = tradePricingProcessor;
        this.tradeService = tradeService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @Path("/generate/{tnxId}")
    public Response generate(@PathParam("tnxId") String tnxId) {
        log.info("Generating margin calls for transaction {}", tnxId);
        List<String> trades = cacheService.remove(tnxId);
        List<Trade> swaps = trades.stream()
                .map(tradeId -> (IRS) tradeService.find(TradeId.fromString(tradeId)))
                .collect(toList());
        Collection<VariationMargin> marginCalls = tradePricingProcessor.process(swaps);
        return Response.status(OK).entity(MarginCallResponse.of(marginCalls)).build();
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
                List<Trade> swaps = trades.stream()
                        .map(tradeId -> tradeService.find(TradeId.fromString(tradeId)))
                        .filter(trade -> trade instanceof IRS)
                        .collect(toList());
                return tradePricingProcessor.process(swaps);
            })
                    .thenApply((result) -> {
                        final AsyncResponse asyncResponse = waiters.get(tnxId);
                        return asyncResponse.resume(Response.status(OK).entity(MarginCallResponse.of(result)).build());
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

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @Path("/generate/swaps")
    public Response generateFromSwaps() {
        log.info("Generating margin calls from all swaps");
        Iterable<IRS> trades = tradeService.findAllIRS();
        Collection<VariationMargin> marginCalls = tradePricingProcessor.process(trades);
        return Response.status(OK).entity(MarginCallResponse.of(marginCalls)).build();
    }
}