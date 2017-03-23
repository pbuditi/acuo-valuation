package com.acuo.valuation.utils;

import com.acuo.common.model.assets.Assets;
import com.acuo.persist.entity.Asset;

import java.util.Currency;

public class AssetsBuilder {

    public static Assets buildAssets(Asset asset)
    {
        Assets assets = new Assets();
        assets.setFitchRating(asset.getRating());
        assets.setCurrency(Currency.getInstance(asset.getCurrency().getCode()));
        assets.setAvailableQuantities(asset.getHolds().getAvailableQuantity());
        assets.setAssetId(asset.getAssetId());
        assets.setICADCode(asset.getICADCode());
        assets.setIdType(asset.getIdType());
        assets.setInternalCost(asset.getHolds().getInternalCost());
        assets.setIssueDate(asset.getIssueDate());
        assets.setMaturityDate(asset.getMaturityDate());
        assets.setMinUnit(asset.getMinUnit());
        assets.setName(asset.getName());
        assets.setOpptCost(asset.getHolds().getOpptCost());
        assets.setParValue(asset.getParValue());
        assets.setType(asset.getType());
        return assets;
    }
}
