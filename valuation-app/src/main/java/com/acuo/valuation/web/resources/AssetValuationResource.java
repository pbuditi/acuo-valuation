package com.acuo.valuation.web.resources;

import com.acuo.common.model.ids.AssetId;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.jackson.AssetValueResult;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.SettlementDateProcessor;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Slf4j
@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
public class AssetValuationResource {

    private final AssetPricingProcessor assetPricingProcessor;
    private final AssetService assetService;
    private final SettlementDateProcessor settlementDateProcessor;

    @Inject
    public AssetValuationResource(AssetPricingProcessor assetPricingProcessor,
                                  AssetService assetService,
                                  SettlementDateProcessor settlementDateProcessor) {
        this.assetPricingProcessor = assetPricingProcessor;
        this.assetService = assetService;
        this.settlementDateProcessor = settlementDateProcessor;
    }


    @GET
    @Path("/price/asset/{id}")
    @Timed
    public Collection<AssetValueResult> priceAssets(@PathParam("id") AssetId assetId) throws Exception {
        Asset asset = assetService.find(assetId);
        final Collection<AssetValue> results = assetPricingProcessor.process(ImmutableList.of(asset));
        return results.stream().map(AssetValueResult::new).collect(toList());
    }

    @GET
    @Path("/price/all")
    @Timed
    public Collection<AssetValueResult> priceAllAssets() throws Exception {
        Iterable<Asset> assets = assetService.findAll(1);
        final Collection<AssetValue> results = assetPricingProcessor.process(assets);
        return results.stream().map(AssetValueResult::new).collect(toList());
    }

    @GET
    @Path("/settlements")
    @Timed
    public Response settlements() throws Exception {
        settlementDateProcessor.process();
        return Response.ok().build();
    }
}
