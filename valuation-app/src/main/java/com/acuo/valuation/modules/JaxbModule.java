package com.acuo.valuation.modules;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.marshal.MarshallerExecutor;
import com.acuo.common.marshal.MarshallerTypes;
import com.acuo.common.marshal.jaxb.JaxbContextFactory;
import com.acuo.common.marshal.jaxb.MoxyJaxbContextFactory;
import com.acuo.common.marshal.json.JsonContextFactory;
import com.acuo.common.marshal.json.MoxyJsonContextFactory;
import com.acuo.valuation.markit.product.swap.IrSwapInput;
import com.acuo.valuation.markit.reports.ReportInput;
import com.acuo.valuation.markit.requests.RequestInput;
import com.acuo.valuation.markit.responses.ResponseInput;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.Arrays;
import java.util.List;

public class JaxbModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JaxbContextFactory.class).to(MoxyJaxbContextFactory.class);
        bind(JsonContextFactory.class).to(MoxyJsonContextFactory.class);
        bind(Marshaller.class).annotatedWith(Names.named("xml")).to(new TypeLiteral<MarshallerExecutor<JaxbContextFactory>>() {
        });
        bind(Marshaller.class).annotatedWith(Names.named("json")).to(new TypeLiteral<MarshallerExecutor<JsonContextFactory>>() {
        });
        bind(ObjectMapper.class).toProvider(JacksonObjectMapperProvider.class);
    }

    @Provides
    @MarshallerTypes
    List<Class<?>> types() {
        return Arrays.asList(new Class<?>[]{ResponseInput.class, RequestInput.class, ReportInput.class, IrSwapInput.class});
    }

}
