package com.acuo.valuation.modules;

import static com.acuo.valuation.PropertiesHelper.ACUO_CONFIG_APPID;
import static com.acuo.valuation.PropertiesHelper.ACUO_CONFIG_ENV;

import javax.inject.Provider;

import com.acuo.valuation.AppId;
import com.acuo.valuation.Configuration;
import com.acuo.valuation.Environment;

public class SystemPropertiesConfigurationProvider implements Provider<Configuration> {

	@Override
	public Configuration get() {
		String id = System.getProperty(ACUO_CONFIG_APPID);
		String env = System.getProperty(ACUO_CONFIG_ENV);
		AppId appId = AppId.of(id);
		return Configuration.builder(appId).with(Environment.lookup(env)).build();
	}
}