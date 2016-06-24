package com.acuo.valuation.modules;

import com.acuo.common.app.Configuration;
import com.acuo.valuation.PropertiesHelper;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.EndPointConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Named;

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
		bind(EndPointConfig.class).toProvider(EndPointConfigProvider.class);
	}

	static class PropertiesModule extends AbstractModule {

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
