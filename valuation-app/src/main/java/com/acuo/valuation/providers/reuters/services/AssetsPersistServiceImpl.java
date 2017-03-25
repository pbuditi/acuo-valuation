package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.*;
import com.acuo.persist.entity.*;
import com.acuo.persist.entity.AssetValuation;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class AssetsPersistServiceImpl implements AssetsPersistService {

    private final AssetService assetService;
    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public AssetsPersistServiceImpl(AssetService assetService, ValuationService valuationService,ValueService valueService)
    {
        this.assetService = assetService;
        this.valuationService = valuationService;
        this.valueService = valueService;
    }


    public void persist(Assets assets)
    {

        Asset asset = assetService.findById(assets.getAssetId());
        AssetValuation assetValuation = null;
        if(asset.getValuation() == null)
        {
            assetValuation = new AssetValuation();
            assetValuation.setAsset(asset);
            valuationService.createOrUpdate(assetValuation);
        }
        else
            assetValuation = (AssetValuation) asset.getValuation();



        Set<Value> values = assetValuation.getValues();
        if(values == null)
            values = new HashSet<>();

        for(Value value : values)
        {
            AssetValue assetValue = (AssetValue)value;
            if(assetValue.getDate().equals(assets.getAssetValuation().getValuationDateTime()))
            {
                valueService.delete(assetValue);
                break;
            }
        }

        AssetValue assetValue = new AssetValue();
        com.acuo.common.model.assets.AssetValuation valuation = assets.getAssetValuation();
        assetValue.setDate(valuation.getValuationDateTime());
        assetValue.setCoupon(valuation.getCoupon());
        assetValue.setNominalCurrency(valuation.getNominalCurrency());
        assetValue.setPrice(valuation.getPrice());
        assetValue.setPriceQuotationType(valuation.getPriceQuotationType());
        assetValue.setReportCurrency(valuation.getReportCurrency());
        assetValue.setValuationDateTime(valuation.getValuationDateTime());
        assetValue.setYield(valuation.getYield());
        assetValue.setValuation(assetValuation);
        valueService.createOrUpdate(assetValue);


    }
}
