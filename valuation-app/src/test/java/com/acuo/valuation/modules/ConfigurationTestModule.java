package com.acuo.valuation.modules;

import com.acuo.common.app.AppId;
import com.acuo.common.app.Configuration;
import com.acuo.common.app.Environment;
import com.acuo.common.app.SecurityKey;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConfigurationTestModule extends AbstractModule {

	private final Configuration configuration;

	public ConfigurationTestModule() {
		configuration = Configuration.builder(AppId.of("valuation-app"))
				.with(Environment.TEST)
				.build();
	}

	@Override
    protected void configure() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Configuration.class).toInstance(configuration);
			}
		});
		install(injector.getInstance(ConfigurationModule.PropertiesModule.class));
        bind(Configuration.class).toInstance(configuration);
	}
}