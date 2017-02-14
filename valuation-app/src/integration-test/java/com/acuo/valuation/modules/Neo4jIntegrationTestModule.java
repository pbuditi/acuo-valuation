package com.acuo.valuation.modules;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

import javax.inject.Provider;
import javax.inject.Singleton;

public class Neo4jIntegrationTestModule extends AbstractModule {

    @Singleton
    static class Neo4jIntegrationTestServer extends AbstractService implements Provider<TestServer> {

        protected TestServer server;

        @Override
        protected void doStart() {
            Configuration configuration = new Configuration();
            configuration.set("driver", "org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
            configuration.set("compiler", "org.neo4j.ogm.compiler.MultiStatementCypherCompiler");
            configuration.set("URI", "bolt://localhost:7687");
            configuration.set("connection.pool.size", "150");
            configuration.set("encryption.level", "NONE");
            Components.configure(configuration);
            server = new TestServer.Builder().enableAuthentication(false).enableBolt(true).transactionTimeoutSeconds(2).build();
            notifyStarted();
        }

        @Override
        protected void doStop() {
            server.shutdown();
            notifyStopped();
        }

        @Override
        public TestServer get() {
            return server;
        }
    }

    @Override
    protected void configure() {
        bind(TestServer.class).toProvider(Neo4jIntegrationTestServer.class);
        Multibinder<Service> services = Multibinder.newSetBinder(binder(), Service.class);
        services.addBinding().to(Neo4jIntegrationTestServer.class);
    }
}
