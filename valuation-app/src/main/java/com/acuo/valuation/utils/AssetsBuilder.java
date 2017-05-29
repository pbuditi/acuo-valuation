package com.acuo.valuation.utils;

import com.acuo.common.model.assets.Assets;
import com.acuo.persist.entity.Asset;

import java.util.Currency;

public class AssetsBuilder {

    public static Assets buildAssets(Asset asset)
    {
        Assets assets = new Assets();
        assets.setFitchRating(asset.getRating());
        assets.setCurrency(asset.getCurrency());
        if(asset.getHolds() != null)
        {
            assets.setAvailableQuantities(asset.getHolds().getAvailableQuantity());
            assets.setInternalCost(asset.getHolds().getInternalCost());
            assets.setOpptCost(asset.getHolds().getOpptCost());
        }

        assets.setAssetId(asset.getAssetId());
        assets.setICADCode(asset.getICADCode());
        assets.setIdType(asset.getIdType());

        assets.setIssueDate(asset.getIssueDate());
        assets.setMaturityDate(asset.getMaturityDate());
        assets.setMinUnit(asset.getMinUnit());
        assets.setName(asset.getName());

        assets.setParValue(asset.getParValue());
        assets.setType(asset.getType());
        assets.setParValue(asset.getParValue());
        return assets;
    }
}
