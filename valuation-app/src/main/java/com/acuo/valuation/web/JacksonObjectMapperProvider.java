package com.acuo.valuation.web;

import com.acuo.valuation.jackson.StrataSerDer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opengamma.strata.basics.currency.Currency;
import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;

import javax.inject.Provider;
import javax.ws.rs.ext.ContextResolver;

public class JacksonObjectMapperProvider implements Provider<ObjectMapper>, ContextResolver<ObjectMapper> {

    final ObjectMapper objectMapper;

    public JacksonObjectMapperProvider() {
        /*objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new CustomModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        objectMapper.setAnnotationIntrospector(new JacksonLombokAnnotationIntrospector());*/

        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
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

    @Override
    public ObjectMapper get() {
        return objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return get();
    }
}
