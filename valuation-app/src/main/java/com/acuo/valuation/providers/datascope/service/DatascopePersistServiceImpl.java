package com.acuo.valuation.providers.datascope.service;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.CurrencyEntity;
import com.acuo.persist.entity.FXRateRelation;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class DatascopePersistServiceImpl implements DatascopePersistService {

    private final CurrencyService currencyService;
    private final AssetService assetService;

    @Inject
    public DatascopePersistServiceImpl(CurrencyService currencyService, AssetService assetService)
    {
        this.currencyService = currencyService;
        this.assetService = assetService;
    }


    public void persistFxRate(List<String> csvLines)
    {
        for(String line : csvLines)
        {
            String[] columns = line.split(",");
            String currencyName = columns[0].substring(0,3);
            String rate = columns[1];
            String lastUpdate = columns[2];
            CurrencyEntity currencyEntity =  currencyService.find(currencyName);

            if(currencyEntity == null)
            {
                currencyEntity = new CurrencyEntity();
                currencyEntity.setCurrencyId(currencyName);
            }
            FXRateRelation fxRateRelation = currencyEntity.getFxRateRelation();
            if(fxRateRelation == null)
            {
                fxRateRelation = new FXRateRelation();
                fxRateRelation.setFrom(currencyEntity);
                fxRateRelation.setTo(currencyService.find("USD"));
                currencyEntity.setFxRateRelation(fxRateRelation);
            }
            if(rate!= null && rate.trim().length() > 0)
                fxRateRelation.setFxRate(1/Double.parseDouble(rate));
            fxRateRelation.setLastUpdate(LocalDateTime.parse(lastUpdate, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
            currencyService.createOrUpdate(currencyEntity);

        }
    }

    public void persistBond(List<String> csvLine)
    {
       for(String line : csvLine)
       {
           String[] columns = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
           String id = columns[3];
           String parValue = columns[1];
           Asset asset = assetService.findById(id);
           if(asset != null && parValue!= null && parValue.trim().length() > 0)
           {
               parValue = parValue.replaceAll("\"", "");
               parValue = parValue.replaceAll(",", "");
               asset.setParValue(Double.parseDouble(parValue));
               assetService.createOrUpdate(asset);
           }


       }

    }
}
