package com.acuo.valuation.modules;

import com.acuo.common.rest.GenericExceptionMapper;
import com.acuo.persist.web.resources.ImportResource;
import com.acuo.valuation.web.resources.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.Properties;

public class ResourcesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UploadResource.class);
        bind(SwapValuationResource.class);
        bind(ModelMapper.class);
        bind(ImportResource.class);
        bind(MarginCallResource.class);
        bind(AssetValuationResource.class);
        bind(DatascopeResource.class);
        bind(FXRatesResource.class);
        bind(GenericExceptionMapper.class);
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