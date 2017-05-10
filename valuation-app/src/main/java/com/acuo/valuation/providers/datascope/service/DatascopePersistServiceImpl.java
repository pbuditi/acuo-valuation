package com.acuo.valuation.providers.datascope.service;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.CurrencyEntity;
import com.acuo.persist.entity.FXRateRelation;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.CurrencyService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxRate;
import lombok.Data;
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
    public DatascopePersistServiceImpl(CurrencyService currencyService, AssetService assetService) {
        this.currencyService = currencyService;
        this.assetService = assetService;
    }

    public void persistFxRate(List<String> csvLines) {
        for (String line : csvLines) {
            final FxRateParser fxRateParser = new FxRateParser();
            if (fxRateParser.parser(line)) {
                FxRate fxRate = fxRateParser.getRate();
                LocalDateTime lastUpdate = fxRateParser.getLastUpdate();

                final Currency base = fxRate.getPair().getBase();
                CurrencyEntity baseEntity = currencyService.find(base.getCode());
                if (baseEntity == null) {
                    log.error("base {} currency not defined in the DB", base);
                    continue;
                }

                final Currency counter = fxRate.getPair().getCounter();
                CurrencyEntity counterEntity = currencyService.find(counter.getCode());
                if (counterEntity == null) {
                    log.error("base {} currency not defined in the DB", base);
                    continue;
                }

                FXRateRelation fxRateRelation = new FXRateRelation();
                fxRateRelation.setFrom(baseEntity);
                fxRateRelation.setTo(counterEntity);
                baseEntity.setFxRateRelation(fxRateRelation);
                fxRateRelation.setFxRate(fxRate.fxRate(fxRate.getPair()));
                fxRateRelation.setLastUpdate(lastUpdate);

                currencyService.createOrUpdate(baseEntity);
            }
        }
    }

    public void persistBond(List<String> csvLine) {
        for (String line : csvLine) {
            String[] columns = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
            String id = columns[3];
            String parValue = columns[1];
            Asset asset = assetService.find(id);
            if (asset != null && parValue != null && parValue.trim().length() > 0) {
                parValue = parValue.replaceAll("\"", "");
                parValue = parValue.replaceAll(",", "");
                asset.setParValue(Double.parseDouble(parValue));
                assetService.createOrUpdate(asset);
            }
        }
    }

    @Data
    static class FxRateParser {

        private static final String PATTERN = ",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

        private FxRate rate;
        private LocalDateTime lastUpdate;

        FxRateParser() {}

        public boolean parser(String line) {
            try {
                String[] columns = line.split(PATTERN);
                String ccy1 = columns[0].substring(0, 3);
                String ccy2 = columns[0].substring(3, 6);
                this.rate = FxRate.parse(ccy1 + "/" + ccy2 + " " + columns[1]);
                this.lastUpdate = LocalDateTime.parse(columns[2], dateTimeFormatter);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        }
    }
}
