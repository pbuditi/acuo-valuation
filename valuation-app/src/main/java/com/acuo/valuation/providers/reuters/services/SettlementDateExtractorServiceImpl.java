package com.acuo.valuation.providers.reuters.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetSettlementDate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SettlementDateExtractorServiceImpl implements SettlementDateExtractorService {

    private final ClientEndPoint<ReutersEndPointConfig> client;
    private final Transformer<Assets> transformer;
    private final Transformer<AssetSettlementDate> resultTransformer;


    @Inject
    public SettlementDateExtractorServiceImpl(ClientEndPoint<ReutersEndPointConfig> client,
                                              @Named("settlementDateTo") Transformer<Assets> transformer,
                                              @Named("settlementDateFrom") Transformer<AssetSettlementDate> resultTransformer) {
        this.client = client;
        this.transformer = transformer;
        this.resultTransformer = resultTransformer;
    }

    public List<AssetSettlementDate> send(List<Assets> assets) {
        if (assets.isEmpty())
            return Collections.emptyList();
        TransformerContext context = new TransformerContext();
        context.setValueDate(LocalDate.now());
        String json = transformer.serialise(assets, context);
        if (log.isDebugEnabled()) {
            log.debug(json);
        }
        String response = ReutersCall.of(client).with("josn", json).create().send();
        if (log.isDebugEnabled()) {
            log.debug(response);
        }
        List<AssetSettlementDate> assetSettlementDates = resultTransformer.deserialiseToList(response.substring(1));
        if (assetSettlementDates == null)
            return Collections.emptyList();
        return assetSettlementDates;
    }
}
