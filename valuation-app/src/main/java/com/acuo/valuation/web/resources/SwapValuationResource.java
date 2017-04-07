package com.acuo.valuation.web.resources;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.reuters.services.AssetsPersistService;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.AssetsBuilder;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.ArrayList;
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
    private final ReutersService reutersService;
    private final AssetsPersistService assetsPersistService;
    private final AssetService assetService;

    @Inject
    public SwapValuationResource(PricingService pricingService,
                                 MarkitValuationProcessor resultProcessor,
                                 VelocityEngine velocityEngine,
                                 ModelMapper mapper,
                                 ReutersService reutersService,
                                 AssetsPersistService assetsPersistService,
                                 AssetService assetService) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
        this.velocityEngine = velocityEngine;
        this.mapper = mapper;
        this.reutersService = reutersService;
        this.assetsPersistService  = assetsPersistService;
        this.assetService = assetService;
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
        log.info("Pricing the trade {}", swapTrade);
        PricingResults result = pricingService.priceSwapTrades(Arrays.asList(swapTrade));
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/swapid/{id}")
    @Timed
    public MarginCallDetail priceBySwapId(@PathParam("id") String id) throws Exception {
        log.info("Pricing the trade {}", id);
        PricingResults results = pricingService.priceTradeIds(ImmutableList.of(id));
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/portfolioid/{id}")
    @Timed
    public MarginCallDetail priceByPortfolio(@PathParam("id") PortfolioId portfolioId) throws Exception {
        log.info("Pricing all trades under the portfolio {}", portfolioId);
        PricingResults results = pricingService.priceTradesUnder(portfolioId);
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/clientid/{id}")
    @Timed
    public PricingResults getPv(@PathParam("id") ClientId clientId) throws Exception {
        log.info("Pricing all trades of client {}", clientId);
        PricingResults result = pricingService.priceTradesOf(clientId);
        return result;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/priceSwapTrades/allBilateralIRS")
    @Timed
    public MarginCallDetail priceallBilateralIRS() throws Exception {
        log.info("Pricing all bilateral trades");
        PricingResults results = pricingService.priceTradesOfType("Bilateral");
        List<VariationMargin> marginCalls = resultProcessor.process(results);
        MarginCallDetail result = MarginCallDetail.of(marginCalls);
        return result;
    }

    @GET
    @Path("/priceAsset/{id}")
    @Timed
    public String priceAssets(@PathParam("id") String assetId) throws Exception
    {
        Asset asset = assetService.findById(assetId);
        Assets assets = AssetsBuilder.buildAssets(asset);
        List<Assets> assetsList = new ArrayList<>();
        assetsList.add(assets);
        List<AssetValuation> response = reutersService.send(assetsList);
        response.stream().forEach(a -> assetsPersistService.persist(a));
        return assetsList.size() + " asset(s) sent and " + response.size() + " valuation received";
    }

    @GET
    @Path("/priceAllAsset")
    @Timed
    public String priceAllAssets() throws Exception
    {
        List<Assets> assetsList = new ArrayList<>();
        Iterable<Asset> assetIterable = assetService.findAll();
        assetIterable.forEach(asset -> assetsList.add(AssetsBuilder.buildAssets(asset)));
        List<AssetValuation> response = reutersService.send(assetsList);
        response.stream().forEach(a -> assetsPersistService.persist(a));
        return assetsList.size() + " asset(s) sent and " + response.size() + " valuation received";
    }
}