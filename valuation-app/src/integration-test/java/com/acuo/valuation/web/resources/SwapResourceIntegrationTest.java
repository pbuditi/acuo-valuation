package com.acuo.valuation.web.resources;

import com.acuo.common.app.ServiceManagerModule;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import org.jboss.resteasy.core.Dispatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationIntegrationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        ParsersModule.class,
        EndPointModule.class,
        ServicesModule.class,
        ServiceManagerModule.class,
        RepositoryModule.class,
        DataLoaderModule.class,
        DataImporterModule.class})
public class SwapResourceIntegrationTest implements WithResteasyFixtures {

    private Dispatcher dispatcher = null;

    @Inject
    private SwapValuationResource resource = null;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
    }

}
