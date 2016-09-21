package com.acuo.valuation.modules;

import com.acuo.valuation.jackson.StrataSerDer;
import com.acuo.valuation.web.resources.SwapValuationResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Properties;

public class ResourcesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SwapValuationResource.class);
        bind(ModelMapper.class);
    }

    @Provides
    public VelocityEngine getEngine() {
        try {
            Properties p = new Properties();
            p.load(getClass().getResourceAsStream("/velocity.properties"));
            VelocityEngine engine = new VelocityEngine(p);
            return engine;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .setAnnotationIntrospector(new JacksonLombokAnnotationIntrospector())
                .registerModule(new JodaModule())
                //.registerModule(new GuavaModule())
                //.registerModule(new Jdk7Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(simpleModule());
    }

    private SimpleModule simpleModule() {
        SimpleModule strataModule = new StrataSerDer().strataModule();
        return strataModule;
    }
}