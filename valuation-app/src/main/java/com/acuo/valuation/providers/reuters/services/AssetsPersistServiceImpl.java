package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.*;
import com.acuo.persist.entity.*;
import com.acuo.persist.entity.AssetValuation;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class AssetsPersistServiceImpl implements AssetsPersistService {

    private final AssetService assetService;
    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public AssetsPersistServiceImpl(AssetService assetService, ValuationService valuationService, ValueService valueService) {
        this.assetService = assetService;
        this.valuationService = valuationService;
        this.valueService = valueService;
    }


    public void persist(Assets assets) {

        Asset asset = assetService.findById(assets.getAssetId());
        AssetValuation assetValuation = null;
        if (asset.getValuation() == null) {
            assetValuation = new AssetValuation();
            assetValuation.setAsset(asset);
            valuationService.createOrUpdate(assetValuation);
        } else
            assetValuation = (AssetValuation) asset.getValuation();


        Set<AssetValueRelation> values = assetValuation.getValues();
        if (values == null)
            values = new HashSet<>();

        for (AssetValueRelation value : values) {
            AssetValue assetValue = value.getValue();
            if (value.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(assets.getAssetValuation().getValuationDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))) {
                valueService.delete(assetValue);
                break;
            }
        }

        AssetValue assetValue = new AssetValue();
        com.acuo.common.model.assets.AssetValuation valuation = assets.getAssetValuation();
        //assetValue.setDate(valuation.getValuationDateTime());
        assetValue.setCoupon(valuation.getCoupon());
        assetValue.setNominalCurrency(valuation.getNominalCurrency());
        assetValue.setPrice(valuation.getPrice());
        assetValue.setPriceQuotationType(valuation.getPriceQuotationType());
        assetValue.setReportCurrency(valuation.getReportCurrency());
        assetValue.setValuationDateTime(valuation.getValuationDateTime());
        assetValue.setYield(valuation.getYield());
        AssetValueRelation valueRelation = new AssetValueRelation();
        valueRelation.setValuation(assetValuation);
        valueRelation.setDateTime(valuation.getValuationDateTime());
        valueRelation.setValue(assetValue);
        assetValue.setValuation(valueRelation);
        valueService.createOrUpdate(assetValue);
        log.info("value created :" + assetValue.toString());


    }
}
