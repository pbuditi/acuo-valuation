package com.acuo.valuation.providers.datascope.service;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.FXRate;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.FXRateService;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class DataScopePersistServiceImpl implements DataScopePersistService {

    private final FXRateService fxRateService;
    private final AssetService assetService;

    @Inject
    public DataScopePersistServiceImpl(FXRateService fxRateService, AssetService assetService) {
        this.fxRateService = fxRateService;
        this.assetService = assetService;
    }

    public void persistFxRate(List<String> csvLines) {
        for (String line : csvLines) {
            final FxRateParser fxRateParser = new FxRateParser();
            if (fxRateParser.parser(line)) {
                FxRate fxRate = fxRateParser.getRate();
                LocalDateTime lastUpdate = fxRateParser.getLastUpdate();

                final Currency base = fxRate.getPair().getBase();
                final Currency counter = fxRate.getPair().getCounter();
                FXRate fxRateRelation = fxRateService.getOrCreate(base, counter);
                // workaround reuters wrong rate for JPYUSD=R
                if (CurrencyPair.of(Currency.USD, Currency.JPY).equals(fxRate.getPair()))
                    fxRateRelation.setValue(fxRate.fxRate(fxRate.getPair()) / 100);
                else
                    fxRateRelation.setValue(fxRate.fxRate(fxRate.getPair()));
                fxRateRelation.setLastUpdate(lastUpdate);

                fxRateService.createOrUpdate(fxRateRelation);
            }
        }
    }

    public void persistBond(List<String> csvLine) {
        for (String line : csvLine) {
            String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
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

        private static final String PATTERN = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

        private FxRate rate;
        private LocalDateTime lastUpdate;

        FxRateParser() {}

        boolean parser(String line) {
            try {
                String[] columns = line.split(PATTERN);
                String counter = columns[0].substring(0, 3);
                String base = columns[0].substring(3, 6);
                this.rate = FxRate.parse(base + "/" + counter + " " + columns[1]);
                this.lastUpdate = LocalDateTime.parse(columns[2], dateTimeFormatter);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        }
    }
}
