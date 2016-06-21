package com.acuo.valuation;

import static java.util.Arrays.asList;

import java.util.Collection;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.app.ResteasyMain;
import com.acuo.valuation.modules.ConfigurationModule;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.web.MOXyCustomJsonProvider;
import com.google.inject.Module;

public class ValuationApp extends ResteasyMain {

	@Override
	public Class<? extends ResteasyConfig> config() {
		return ResteasyConfigImpl.class;
	}

	@Override
	public Collection<Class<?>> providers() {
		return asList(MOXyCustomJsonProvider.class);
	}

	@Override
	public Collection<Module> modules() {
		return asList(new JaxbModule(), new ConfigurationModule(), new ServicesModule(), new ResourcesModule());
	}

	public static void main(String[] args) throws Exception {
		new ValuationApp();
	}
}