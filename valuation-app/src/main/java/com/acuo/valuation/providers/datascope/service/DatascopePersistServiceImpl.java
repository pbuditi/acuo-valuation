package com.acuo.valuation.providers.datascope.service;

import com.acuo.persist.entity.CurrencyEntity;
import com.acuo.persist.entity.FXRateRelation;
import com.acuo.persist.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class DatascopePersistServiceImpl implements DatascopePersistService {

    private final CurrencyService currencyService;

    @Inject
    public DatascopePersistServiceImpl(CurrencyService currencyService)
    {
        this.currencyService = currencyService;
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
}
