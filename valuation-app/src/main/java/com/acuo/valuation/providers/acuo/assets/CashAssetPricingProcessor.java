package com.acuo.valuation.providers.acuo.assets;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.ids.AssetId;
import com.acuo.persist.services.CurrencyService;
import com.acuo.valuation.providers.reuters.services.AssetsPersistService;
import com.opengamma.strata.basics.currency.Currency;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.DataScope;
import static java.util.stream.Collectors.toList;

public class CashAssetPricingProcessor extends AbstractAssetPricingProcessor {

    private final CurrencyService currencyService;
    private final AssetsPersistService persistService;

    private static final Predicate<Asset> cashAssets = asset -> {
        final PricingSource pricingSource = asset.getPricingSource();
        return DataScope.equals(pricingSource.getName());
    };

    @Inject
    public CashAssetPricingProcessor(CurrencyService currencyService,
                                     AssetsPersistService persistService) {
        this.currencyService = currencyService;
        this.persistService = persistService;
    }

    @Override
    public Collection<AssetValue> process(Iterable<Asset> assets) {
        final Collection<AssetValue> results = internal(assets);
        if (next != null) {
            results.addAll(next.process(assets));
        }
        return results;
    }

    private Collection<AssetValue> internal(Iterable<Asset> assets) {
        return StreamSupport.stream(assets.spliterator(), false)
                .filter(cashAssets)
                .map(asset -> {
                    final AssetId assetId = AssetId.fromString(asset.getAssetId());
                    return ImmutablePair.of(assetId, createAssetValue(asset));
                })
                .map(value -> persistService.persist(value.getLeft(), value.getRight()))
                .collect(toList());

    }

    private AssetValue createAssetValue(Asset asset) {
        AssetValue assetValue = new AssetValue();
        assetValue.setCoupon(0.0d);
        Currency currency = asset.getCurrency();
        assetValue.setNominalCurrency(currency);
        Double division = currencyService.getFXValue(currency);
        assetValue.setUnitValue(division);
        assetValue.setPriceQuotationType("FX");
        assetValue.setReportCurrency(Currency.USD);
        assetValue.setValuationDateTime(LocalDate.now());
        assetValue.setYield(0.0d);
        return assetValue;
    }
}
