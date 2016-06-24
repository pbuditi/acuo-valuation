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
		install(injector.getInstance(MarkitModule.class));
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

	static class MarkitModule extends AbstractModule {

		@Inject
		@Named(PropertiesHelper.ACUO_VALUATION_MARKIT_HOST)
		private String markitHost;

		@Inject
		@Named(PropertiesHelper.ACUO_VALUATION_MARKIT_USERNAME)
		private String markitUsername;

		@Inject
		@Named(PropertiesHelper.ACUO_VALUATION_MARKIT_PASSWORD)
		private String markitPassword;

		@Inject
		@Named(PropertiesHelper.ACUO_VALUATION_MARKIT_RETRY_DELAY)
		private Long markitRetryDelay;

		@Override
		protected void configure() {
			MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(markitHost,
					markitUsername, markitPassword, markitRetryDelay);
			bind(EndPointConfig.class).toInstance(markitEndPointConfig);
		}
	}
}
