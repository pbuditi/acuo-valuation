package com.acuo.valuation.modules;

import com.acuo.common.app.Configuration;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
	}

}
