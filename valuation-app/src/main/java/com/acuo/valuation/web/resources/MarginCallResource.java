package com.acuo.valuation.web.resources;

import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.jackson.PortfolioIds;
import com.acuo.valuation.providers.acuo.PortfolioValuationProcessor;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import com.acuo.valuation.services.PortfolioManager;
import com.acuo.valuation.services.TradeCacheService;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
@Path("/calls")
public class MarginCallResource {

    private final static Map<String, AsyncResponse> waiters = new ConcurrentHashMap<>();

    private final TradeCacheService cacheService;
    private final TradePricingProcessor tradePricingProcessor;
    private final TradeService<Trade> tradeService;

    private final MarginCallService marginCallService;

    private final PortfolioValuationProcessor portfolioValuationProcessor;
    private final PortfolioManager portfolioManager;

    @Inject
    public MarginCallResource(TradeCacheService cacheService,
                              TradePricingProcessor tradePricingProcessor,
                              TradeService<Trade> tradeService,
                              MarginCallService marginCallService,
                              PortfolioValuationProcessor portfolioValuationProcessor,
                              PortfolioManager portfolioManager) {
        this.cacheService = cacheService;
        this.tradePricingProcessor = tradePricingProcessor;
        this.tradeService = tradeService;
        this.marginCallService = marginCallService;
        this.portfolioValuationProcessor = portfolioValuationProcessor;
        this.portfolioManager = portfolioManager;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @Path("/generate/{tnxId}")
    public Response generate(@PathParam("tnxId") String tnxId) {
        log.info("Generating margin calls for transaction {}", tnxId);
        List<String> ids = cacheService.remove(tnxId);
        List<Trade> trades = ids.stream()
                .map(tradeId ->  tradeService.find(TradeId.fromString(tradeId)))
                .collect(toList());
        Collection<MarginCall> marginCalls = tradePricingProcessor.process(trades);
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
        Iterable<Trade> trades = tradeService.findAll(2);
        Collection<MarginCall> marginCalls = tradePricingProcessor.process(trades);
        return Response.status(OK).entity(MarginCallResponse.of(marginCalls)).build();
    }

    @POST
    @Path("/split/portfolios")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response splitMarginCallByPortfolios(PortfolioIds portfolioIds) throws Exception {
        log.info("Pricing all trades under the portfolios {}", portfolioIds);
        LocalDate valuationDate = LocalDateUtils.valuationDate();
        List<Portfolio> portfolios = portfolioManager.valueMissing(portfolioIds.getIds(), valuationDate);
        final MarginCallResponse response = portfolioManager.split(portfolios, valuationDate);
        return Response.status(CREATED).entity(response).build();
    }

    @POST
    @Path("/generate/portfolios")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response generateMarginCallForPortfolio(PortfolioIds portfolioIds) throws Exception {
        log.info("generate margin calls the portfolios {}", portfolioIds);
        Set<PortfolioId> portfolios = portfolioIds.getIds().stream().map(PortfolioId::fromString).collect(toSet());
        List<MarginCall> marginCalls = portfolioValuationProcessor.process(portfolios);
        marginCalls = marginCalls.stream().map(marginCall -> marginCallService.find(marginCall.getItemId(), 4)).collect(toList());
        return Response.status(CREATED).entity(MarginCallResponse.of(marginCalls)).build();
    }
}