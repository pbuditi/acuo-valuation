package com.acuo.valuation.modules;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.services.ClarusTransformer;
import com.acuo.collateral.transform.services.MarkitTransformer;
import com.acuo.collateral.transform.trace.transformer_valuations.Mapper;
import com.acuo.common.marshal.Marshaller;
import com.acuo.common.marshal.MarshallerExecutor;
import com.acuo.common.marshal.MarshallerTypes;
import com.acuo.common.marshal.jaxb.JaxbContextFactory;
import com.acuo.common.marshal.jaxb.MoxyJaxbContextFactory;
import com.acuo.common.marshal.json.JsonContextFactory;
import com.acuo.common.marshal.json.MoxyJsonContextFactory;
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
        ClarusTransformer<SwapTrade> clarusTransformer = new ClarusTransformer<>(new Mapper());
        bind(new TypeLiteral<Transformer<SwapTrade>>() {
        }).annotatedWith(Names.named("clarus")).toInstance(clarusTransformer);
        MarkitTransformer<SwapTrade> markitTransformer = new MarkitTransformer<>(new Mapper());
        bind(new TypeLiteral<Transformer<SwapTrade>>() {
        }).annotatedWith(Names.named("markit")).toInstance(markitTransformer);
    }

    @Provides
    @MarshallerTypes
    List<Class<?>> types() {
        return Arrays.asList(new Class<?>[]{ResponseInput.class, ReportInput.class});
    }

}
