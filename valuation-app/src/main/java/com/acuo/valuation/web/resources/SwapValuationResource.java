package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.jackson.PortfolioIds;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.calls.MarkitCallGenerator;
import com.acuo.valuation.providers.acuo.calls.MarkitCallSimulator;
import com.acuo.valuation.providers.acuo.trades.PortfolioPriceProcessor;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
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
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

    private final PricingService pricingService;
    private final TradeService<Trade> tradeService;
    private final TradePricingProcessor tradePricingProcessor;
    private final VelocityEngine velocityEngine;
    private final PortfolioPriceProcessor portfolioPriceProcessor;
    private final MarkitCallGenerator markitCallGenerator;
    private final MarkitCallSimulator markitCallSimulator;
    private final PortfolioService portfolioService;
    private final ValuationService valuationService;

    @Inject
    public SwapValuationResource(PricingService pricingService,
                                 TradeService<Trade> tradeService,
                                 TradePricingProcessor tradePricingProcessor,
                                 VelocityEngine velocityEngine,
                                 PortfolioPriceProcessor portfolioPriceProcessor,
                                 MarkitCallGenerator markitCallGenerator,
                                 MarkitCallSimulator markitCallSimulator,
                                 PortfolioService portfolioService,
                                 ValuationService valuationService) {
        this.pricingService = pricingService;
        this.tradeService = tradeService;
        this.tradePricingProcessor = tradePricingProcessor;
        this.velocityEngine = velocityEngine;
        this.portfolioPriceProcessor = portfolioPriceProcessor;
        this.markitCallGenerator = markitCallGenerator;
        this.markitCallSimulator = markitCallSimulator;
        this.portfolioService = portfolioService;
        this.valuationService = valuationService;
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
    @Path("/priceSwapTrades/swapid/{id}")
    @Timed
    public MarginCallResponse priceBySwapId(@PathParam("id") String id) throws Exception {
        log.info("Pricing the trade {}", id);
        List<Trade> swaps = ImmutableList.of(id).stream()
                .map(tradeId -> (IRS) tradeService.find(TradeId.fromString(tradeId)))
                .collect(toList());
        Collection<MarginCall> marginCalls = tradePricingProcessor.process(swaps);
        return MarginCallResponse.of(marginCalls);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/portfolioid/{id}")
    @Timed
    public MarginCallResponse priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        log.info("Pricing all trades under the portfolio {}", portfolioId);
        List<Trade> swaps = ImmutableList.of(portfolioId).stream()
                .map(tradeId -> (IRS) tradeService.findByPortfolioId(portfolioId))
                .collect(toList());
        Collection<MarginCall> marginCalls = tradePricingProcessor.process(swaps);
        return MarginCallResponse.of(marginCalls);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/clientid/{id}")
    @Timed
    public MarkitResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        log.info("Pricing all trades of client {}", clientId);
        return pricingService.priceTradesOf(clientId);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/allBilateralIRS")
    @Timed
    public MarginCallResponse priceallBilateralIRS() throws Exception {
        log.info("Pricing all bilateral trades");
        Iterable<IRS> allIRS = tradeService.findAllIRS();
        Collection<MarginCall> marginCalls = tradePricingProcessor.process(allIRS);
        return MarginCallResponse.of(marginCalls);
    }

    @POST
    @Path("/priceSwapTrades/portfolio")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response pricePortfolio(PortfolioIds portfolioIds) throws Exception {
        log.info("Pricing all trades under the portfolios {}", portfolioIds);
        portfolioPriceProcessor.process(portfolioIds.getIds().stream().map(PortfolioId::fromString).collect(toList()));
        final MarginCallResponse  response = MarginCallResponse.ofPortfolio(portfolioIds.getIds().stream().map(PortfolioId::fromString).map(id -> portfolioService.find(id, 2)).collect(Collectors.toList()), tradeService, valuationService);
        return Response.status(CREATED).entity(response).build();
    }

    @POST
    @Path("/priceSwapTrades/generatemc")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response generateMarginCallForPortfolio(PortfolioIds portfolioIds) throws Exception
    {
        log.info("generate margin calls the portfolios {}", portfolioIds);
        List<PortfolioId> portfolioIdList = portfolioIds.getIds().stream().map(s -> PortfolioId.fromString(s)).collect(toList());
        List<MarginCall> marginCalls = markitCallGenerator.generateForPortfolios(portfolioIdList);
        marginCalls.addAll(markitCallSimulator.generateForPortfolios(portfolioIdList));
        return Response.status(CREATED).entity(MarginCallResponse.of(marginCalls)).build();
    }

}