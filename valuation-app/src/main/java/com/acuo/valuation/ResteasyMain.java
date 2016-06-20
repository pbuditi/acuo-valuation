package com.acuo.valuation;

import java.util.logging.LogManager;

import javax.servlet.ServletContextListener;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
//import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.providers.html.HtmlServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.acuo.common.http.server.BinderProviderCapture;
import com.acuo.common.http.server.HttpServerWrapperConfig;
import com.acuo.common.http.server.HttpServerWrapperFactory;
import com.acuo.common.http.server.HttpServerWrapperModule;
import com.acuo.valuation.modules.ConfigurationModule;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.web.MOXyCustomJsonProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class ResteasyMain {

	public static void main(String[] args) throws Exception {

		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();

		BinderProviderCapture<? extends ServletContextListener> listenerProvider = new BinderProviderCapture<>(
				GuiceResteasyBootstrapServletContextListener.class);

		Injector injector = Guice.createInjector(new ServiceModule(listenerProvider));

		ResteasyConfig instance = injector.getInstance(ResteasyConfig.class);
		HttpServerWrapperConfig config = instance.getConfig();

		config.addServletContextListenerProvider(listenerProvider);

		injector.getInstance(HttpServerWrapperFactory.class).getHttpServerWrapper(config).start();
	}

	private static class ServiceModule extends AbstractModule {

		private final BinderProviderCapture<?> listenerProvider;

		public ServiceModule(BinderProviderCapture<?> listenerProvider) {
			this.listenerProvider = listenerProvider;
		}

		@Override
		protected void configure() {
			binder().requireExplicitBindings();

			bind(MOXyCustomJsonProvider.class);

			install(new HttpServerWrapperModule());
			install(new JaxbModule());
			install(new ConfigurationModule());
			// install(new ServicesModule());
			install(new ResourcesModule());

			bind(GuiceResteasyBootstrapServletContextListener.class);

			bind(ResteasyConfig.class).to(ResteasyConfigImpl.class).in(Singleton.class);

			install(new ServletModule() {
				@Override
				protected void configureServlets() {
					serve("/*").with(HtmlServletDispatcher.class);
					bind(HtmlServletDispatcher.class).in(Scopes.SINGLETON);
				}
			});

			listenerProvider.saveProvider(binder());
		}
	}
}