package com.acuo.valuation.modules;

import com.acuo.common.app.Configuration;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.providers.reuters.services.ReutersEndPointConfig;
import com.acuo.valuation.utils.PropertiesHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Inject;

public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Configuration.class).toProvider(SystemPropertiesConfigurationProvider.class);
            }
        });
        install(injector.getInstance(PropertiesModule.class));
        bind(Configuration.class).toProvider(SystemPropertiesConfigurationProvider.class);
        bind(MarkitEndPointConfig.class);
        bind(ClarusEndPointConfig.class);
        bind(ReutersEndPointConfig.class);
        bind(DataScopeEndPointConfig.class);
    }

    public static class PropertiesModule extends AbstractModule {

        @Inject
        private Configuration configuration;

        @Override
        protected void configure() {
            PropertiesHelper helper = PropertiesHelper.of(configuration);
            install(new com.smokejumperit.guice.properties.PropertiesModule(helper.getDefaultProperties(),
                    helper.getOverrides()));
        }
    }
}
