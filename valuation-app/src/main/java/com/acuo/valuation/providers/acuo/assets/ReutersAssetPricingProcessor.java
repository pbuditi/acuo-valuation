package com.acuo.valuation.providers.acuo.assets;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.services.AssetValuationService;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.utils.AssetsBuilder;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Reuters;
import static java.util.stream.Collectors.toList;

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
        Collection<AssetValue> result = internal(assets);
        if (next != null) {
            result.addAll(next.process(assets));
        }
        return result;
    }

    private Collection<AssetValue> internal(Iterable<Asset> assets) {
        final List<Assets> list = StreamSupport.stream(assets.spliterator(), false)
                .filter(reutersPredicate)
                .map(AssetsBuilder::buildAssets)
                .collect(toList());
        List<AssetValuation> results = reutersService.send(list);
        return assetsPersistService.persist(results);
    }
}