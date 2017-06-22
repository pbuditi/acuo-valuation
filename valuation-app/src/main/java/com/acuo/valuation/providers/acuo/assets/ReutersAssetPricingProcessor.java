package com.acuo.valuation.providers.acuo.assets;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.services.AssetValuationService;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.utils.AssetsBuilder;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Reuters;
import static java.util.stream.Collectors.toList;

@Slf4j
public class ReutersAssetPricingProcessor extends AbstractAssetPricingProcessor {

    private final ReutersService reutersService;
    private final AssetValuationService assetsPersistService;

    private static final Predicate<Asset> reutersPredicate = asset -> {
        final PricingSource pricingSource = asset.getPricingSource();
        return Reuters.equals(pricingSource.getName());
    };

    @Inject
    public ReutersAssetPricingProcessor(ReutersService reutersService,
                                        AssetValuationService assetsPersistService) {
        this.reutersService = reutersService;
        this.assetsPersistService = assetsPersistService;
    }

    @Override
    public Collection<AssetValue> process(Iterable<Asset> assets) {
        log.info("processing {} assets", Iterables.size(assets));
        Collection<AssetValue> results = internal(assets);
        log.info("generated {} results", results.size());
        if (next != null) {
            results.addAll(next.process(assets));
        }
        return results;
    }

    private Collection<AssetValue> internal(Iterable<Asset> assets) {
        final List<Assets> list = StreamSupport.stream(assets.spliterator(), false)
                .filter(reutersPredicate)
                .filter(asset -> !"FR0010482547".equals(asset.getAssetId()))
                .map(AssetsBuilder::buildAssets)
                .collect(toList());
        List<AssetValuation> results = reutersService.send(list);
        return assetsPersistService.persist(results);
    }
}