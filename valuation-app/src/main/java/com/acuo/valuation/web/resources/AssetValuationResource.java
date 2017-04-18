package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.jackson.AssetValueResult;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Slf4j
@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
public class AssetValuationResource {

    private final AssetPricingProcessor assetPricingProcessor;
    private final AssetService assetService;

    @Inject
    public AssetValuationResource(AssetPricingProcessor assetPricingProcessor,
                                  AssetService assetService) {
        this.assetPricingProcessor = assetPricingProcessor;
        this.assetService = assetService;
    }


    @GET
    @Path("/price/asset/{id}")
    @Timed
    public Collection<AssetValueResult> priceAssets(@PathParam("id") String assetId) throws Exception {
        Asset asset = assetService.findById(assetId);
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
}
