package com.acuo.valuation.web.resources;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.providers.reuters.services.AssetsPersistService;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.utils.AssetsBuilder;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetValuationResource {

    private final
    private final ReutersService reutersService;
    private final AssetsPersistService assetsPersistService;
    private final AssetService assetService;

    @Inject
    public AssetValuationResource(ReutersService reutersService,
                                  AssetsPersistService assetsPersistService,
                                  AssetService assetService) {
        this.reutersService = reutersService;
        this.assetsPersistService = assetsPersistService;
        this.assetService = assetService;
    }


    @GET
    @Path("/priceAsset/{id}")
    @Timed
    public String priceAssets(@PathParam("id") String assetId) throws Exception {
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
    public String priceAllAssets() throws Exception {
        List<Assets> assetsList = new ArrayList<>();
        Iterable<Asset> assetIterable = assetService.findAll();
        assetIterable.forEach(asset -> assetsList.add(AssetsBuilder.buildAssets(asset)));
        List<AssetValuation> response = reutersService.send(assetsList);
        response.stream().forEach(a -> assetsPersistService.persist(a));
        return assetsList.size() + " asset(s) sent and " + response.size() + " valuation received";
    }

}
