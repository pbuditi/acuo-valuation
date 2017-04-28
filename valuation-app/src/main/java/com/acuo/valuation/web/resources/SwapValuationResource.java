package com.acuo.valuation.web.resources;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

    private final PricingService pricingService;
    private final MarkitValuationProcessor resultProcessor;
    private final VelocityEngine velocityEngine;
    private final ModelMapper mapper;


    @Inject
    public SwapValuationResource(PricingService pricingService,
                                 MarkitValuationProcessor resultProcessor,
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
    public MarkitResults price(SwapTrade swapTrade) throws Exception {
        log.info("Pricing the trade {}", swapTrade);
        MarkitResults result = pricingService.priceSwapTrades(Arrays.asList(swapTrade));
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/swapid/{id}")
    @Timed
    public MarginCallResponse priceBySwapId(@PathParam("id") String id) throws Exception {
        log.info("Pricing the trade {}", id);
        MarkitResults results = pricingService.priceTradeIds(ImmutableList.of(id));
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallResponse result = MarginCallResponse.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/portfolioid/{id}")
    @Timed
    public MarginCallResponse priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        log.info("Pricing all trades under the portfolio {}", portfolioId);
        MarkitResults results = pricingService.priceTradesUnder(portfolioId);
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallResponse result = MarginCallResponse.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/clientid/{id}")
    @Timed
    public MarkitResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        log.info("Pricing all trades of client {}", clientId);
        MarkitResults result = pricingService.priceTradesOf(clientId);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/allBilateralIRS")
    @Timed
    public MarginCallResponse priceallBilateralIRS() throws Exception {
        log.info("Pricing all bilateral trades");
        MarkitResults results = pricingService.priceTradesOfType("Bilateral");
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallResponse result = MarginCallResponse.of(marginCalls);
        return result;
    }
}