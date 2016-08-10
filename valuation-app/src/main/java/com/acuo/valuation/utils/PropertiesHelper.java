package com.acuo.valuation.utils;

import com.acuo.common.app.AppId;
import com.acuo.common.app.Configuration;
import com.acuo.common.app.Environment;
import com.acuo.common.util.ArgChecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper {

    private static final String DEFAULTS_PROPERTIES_TEMPLATE = "/%s.properties";
    private static final String OVERRIDES_PROPERTIES_TEMPLATE = "/%s-%s.properties";

    public static final String ACUO_CONFIG_APPID = "acuo.config.appid";
    public static final String ACUO_CONFIG_ENV = "acuo.config.env";
    public static final String ACUO_VALUATION_APP_HOST = "acuo.webapp.host";
    public static final String ACUO_VALUATION_APP_PORT = "acuo.webapp.port";
    public static final String ACUO_VALUATION_APP_DIR = "acuo.webapp.dir";
    public static final String ACUO_VALUATION_APP_CTX_PATH = "acuo.webapp.context.path";
    public static final String ACUO_VALUATION_APP_REST_MAPPING_PREFIX = "acuo.webapp.rest.servlet.mapping.prefix";
    public static final String ACUO_VALUATION_MARKIT_HOST = "acuo.markit.host";
    public static final String ACUO_VALUATION_MARKIT_USERNAME = "acuo.markit.username";
    public static final String ACUO_VALUATION_MARKIT_PASSWORD = "acuo.markit.password";
    public static final String ACUO_VALUATION_MARKIT_RETRY_DELAY = "acuo.markit.retry.delay";
    public static final String ACUO_VALUATION_MARKIT_CONNECTION_TIMEOUT = "acuo.markit.connection.timeout";

    public static final String ACUO_VALUATION_CLARUS_HOST = "acuo.clarus.host";
    public static final String ACUO_VALUATION_CLARUS_API_KEY = "acuo.clarus.api.key";
    public static final String ACUO_VALUATION_CLARUS_API_SECRET = "acuo.clarus.api.secret";
    public static final String ACUO_VALUATION_CLARUS_CONNECTION_TIMEOUT = "acuo.clarus.connection.timeout";

    private final Configuration configuration;

    private PropertiesHelper(Configuration configuration) {
        this.configuration = configuration;
    }

    public static PropertiesHelper of(Configuration configuration) {
        ArgChecker.notNull(configuration, "configuration");
        return new PropertiesHelper(configuration);
    }

    public Properties getOverrides() {
        return getPropertiesFrom(overrideFilePath());
    }

    public Properties getDefaultProperties() {
        return getPropertiesFrom(defaultFilePath());
    }

    private String overrideFilePath() {
        AppId appId = configuration.getAppId();
        Environment environment = configuration.getEnvironment();
        return String.format(OVERRIDES_PROPERTIES_TEMPLATE, appId.toString(), environment.toString());
    }

    private String defaultFilePath() {
        AppId appId = configuration.getAppId();
        return String.format(DEFAULTS_PROPERTIES_TEMPLATE, appId.toString());
    }

    private Properties getPropertiesFrom(String propertiesFilePath) {
        final Properties properties = new Properties();
        try (final InputStream stream = PropertiesHelper.class.getResourceAsStream(propertiesFilePath)) {
            if (stream != null)
                properties.load(stream);
        } catch (IOException e) {
        }
        return properties;
    }
}
