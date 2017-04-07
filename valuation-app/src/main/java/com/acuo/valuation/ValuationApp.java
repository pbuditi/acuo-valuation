package com.acuo.valuation;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.app.ResteasyMain;
import com.acuo.common.security.EncryptionModule;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.HealthChecksModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ParsersModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.web.ObjectMapperContextResolver;
import com.google.inject.Module;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import static java.util.Arrays.asList;

@Slf4j
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
                    new HealthChecksModule(),
                    new Neo4jPersistModule(),
                    new Neo4jPersistModule(),
                    new DataLoaderModule(),
                    new DataImporterModule(),
                    new ImportServiceModule(),
                    new RepositoryModule());
    }

    public static void main(String[] args) throws Exception {

        ValuationApp valuationApp = new ValuationApp();
        valuationApp.startAsync();
    }
}