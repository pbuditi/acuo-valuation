package com.acuo.valuation;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.app.ResteasyMain;
import com.acuo.common.security.EncryptionModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.web.ObjectMapperContextResolver;
import com.google.inject.Module;

import java.util.Collection;

import static java.util.Arrays.asList;

public class ValuationApp extends ResteasyMain {

    @Override
    public Class<? extends ResteasyConfig> config() {
        return ResteasyConfigImpl.class;
    }

    @Override
    public Collection<Class<?>> providers() {
        return asList(ObjectMapperContextResolver.class);
    }

    @Override
    public Collection<Module> modules() {
        return asList(new MappingModule(),
                    new EncryptionModule(),
                    new ConfigurationModule(),
                    new ParsersModule(),
                    new EndPointModule(),
                    new ServicesModule(),
                    new ResourcesModule(),
                    new HealthChecksModule());
    }

    public static void main(String[] args) throws Exception {
        ValuationApp valuationApp = new ValuationApp();
        valuationApp.startAsync();
    }
}