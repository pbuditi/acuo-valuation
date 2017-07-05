package com.acuo.valuation.modules;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.services.*;
import com.acuo.collateral.transform.trace.transformer_valuations.Mapper;
import com.acuo.common.marshal.Marshaller;
import com.acuo.common.marshal.MarshallerExecutor;
import com.acuo.common.marshal.MarshallerTypes;
import com.acuo.common.marshal.jaxb.JaxbContextFactory;
import com.acuo.common.marshal.jaxb.MoxyJaxbContextFactory;
import com.acuo.common.marshal.json.JsonContextFactory;
import com.acuo.common.marshal.json.MoxyJsonContextFactory;
import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetSettlementDate;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.model.results.TradeValuation;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.providers.markit.protocol.reports.ReportInput;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseInput;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.Arrays;
import java.util.List;

public class MappingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JaxbContextFactory.class).to(MoxyJaxbContextFactory.class);
        bind(JsonContextFactory.class).to(MoxyJsonContextFactory.class);
        bind(Marshaller.class).annotatedWith(Names.named("xml")).to(new TypeLiteral<MarshallerExecutor<JaxbContextFactory>>() {
        });
        bind(Marshaller.class).annotatedWith(Names.named("json")).to(new TypeLiteral<MarshallerExecutor<JsonContextFactory>>() {
        });

        bind(ObjectMapper.class).toProvider(JacksonObjectMapperProvider.class);

        ClarusTransformer<com.acuo.common.model.trade.Trade> clarusTransformer = new ClarusTransformer<>(new Mapper());
        MarkitTransformer<com.acuo.common.model.trade.Trade> markitTransformer = new MarkitTransformer<>(new Mapper());
        ReutersTransformer<Assets> assetsAssetsTransformer = new ReutersTransformer<>();
        ReutersTransformer<AssetValuation> assetValuationReutersTransformer = new ReutersTransformer<>();
        PortfolioImportTransformer<com.acuo.common.model.trade.Trade> portfolioImportTransformer = new PortfolioImportTransformer<>(new Mapper());
        TradeValuationTransformer<TradeValuation> tradeValuationTransformer = new TradeValuationTransformer<>(new Mapper());
        SettlementDateTransformer<Assets> settlementDateTransformer = new SettlementDateTransformer<>();
        SettlementDateTransformer<AssetSettlementDate> settlementDateSettlementDateTransformer = new SettlementDateTransformer<>();

        bind(new TypeLiteral<Transformer<com.acuo.common.model.trade.Trade>>() {}).annotatedWith(Names.named("clarus")).toInstance(clarusTransformer);
        bind(new TypeLiteral<Transformer<com.acuo.common.model.trade.Trade>>() {}).annotatedWith(Names.named("markit")).toInstance(markitTransformer);
        bind(new TypeLiteral<Transformer<Assets>>() {}).annotatedWith(Names.named("assets")).toInstance(assetsAssetsTransformer);
        bind(new TypeLiteral<Transformer<AssetValuation>>() {}).annotatedWith(Names.named("assetValuation")).toInstance(assetValuationReutersTransformer);
        bind(new TypeLiteral<Transformer<com.acuo.common.model.trade.Trade>>() {}).annotatedWith(Names.named("portfolio")).toInstance(portfolioImportTransformer);
        bind(new TypeLiteral<Transformer<TradeValuation>>() {}).annotatedWith(Names.named("tradeValuation")).toInstance(tradeValuationTransformer);
        bind(new TypeLiteral<Transformer<Assets>>() {}).annotatedWith(Names.named("settlementDateTo")).toInstance(settlementDateTransformer);
        bind(new TypeLiteral<Transformer<AssetSettlementDate>>() {}).annotatedWith(Names.named("settlementDateFrom")).toInstance(settlementDateSettlementDateTransformer);
    }

    @Provides
    @MarshallerTypes
    List<Class<?>> types() {
        return Arrays.asList(ResponseInput.class, ReportInput.class);
    }

}
