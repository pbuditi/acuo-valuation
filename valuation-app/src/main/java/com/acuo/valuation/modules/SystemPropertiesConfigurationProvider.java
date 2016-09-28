package com.acuo.valuation.modules;

import com.acuo.common.app.AppId;
import com.acuo.common.app.Configuration;
import com.acuo.common.app.Environment;
import com.acuo.common.app.SecurityKey;

import javax.inject.Provider;

import static com.acuo.valuation.utils.PropertiesHelper.ACUO_CONFIG_APPID;
import static com.acuo.valuation.utils.PropertiesHelper.ACUO_CONFIG_ENV;
import static com.acuo.valuation.utils.PropertiesHelper.ACUO_SECURITY_KEY;

public class SystemPropertiesConfigurationProvider implements Provider<Configuration> {

    @Override
    public Configuration get() {
        String id = System.getProperty(ACUO_CONFIG_APPID);
        String env = System.getProperty(ACUO_CONFIG_ENV);
        String key = System.getProperty(ACUO_SECURITY_KEY);
        AppId appId = AppId.of(id);
        return Configuration.builder(appId)
                            .with(Environment.lookup(env))
                            .with(SecurityKey.of(key))
                            .build();
    }
}