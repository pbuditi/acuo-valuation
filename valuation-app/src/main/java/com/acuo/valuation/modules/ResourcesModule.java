package com.acuo.valuation.modules;

import com.acuo.valuation.jackson.StrataSerDer;
import com.acuo.valuation.web.resources.*;
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
        bind(UploadResource.class);
        bind(SwapValuationResource.class);
        bind(ModelMapper.class);
        bind(ImportResource.class);
        bind(ClarusValuationResource.class);
        bind(Neo4jConnectionExceptionHandler.class);
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
}