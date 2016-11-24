package com.acuo.valuation.utils;

import com.acuo.common.app.Configuration;
import com.acuo.common.util.ArgChecker;
import com.acuo.common.util.BasePropertiesHelper;

public class PropertiesHelper extends BasePropertiesHelper {

    public static final String ACUO_CONFIG_APPID = "acuo.config.appid";
    public static final String ACUO_CONFIG_ENV = "acuo.config.env";

    public static final String ACUO_SECURITY_KEY = "acuo.security.key";

    public static final String ACUO_VALUATION_APP_HOST = "acuo.webapp.host";
    public static final String ACUO_VALUATION_APP_PORT = "acuo.webapp.port";
    public static final String ACUO_VALUATION_APP_DIR = "acuo.webapp.dir";
    public static final String ACUO_VALUATION_APP_CTX_PATH = "acuo.webapp.context.path";
    public static final String ACUO_VALUATION_APP_REST_MAPPING_PREFIX = "acuo.webapp.rest.servlet.mapping.prefix";

    public static final String ACUO_VALUATION_MARKIT_SCHEME = "acuo.markit.scheme";
    public static final String ACUO_VALUATION_MARKIT_HOST = "acuo.markit.host";
    public static final String ACUO_VALUATION_MARKIT_PORT = "acuo.markit.port";
    public static final String ACUO_VALUATION_MARKIT_UPLOAD_PATH = "acuo.markit.upload.path";
    public static final String ACUO_VALUATION_MARKIT_DOWNLOAD_PATH = "acuo.markit.download.path";
    public static final String ACUO_VALUATION_MARKIT_USERNAME = "acuo.markit.username";
    public static final String ACUO_VALUATION_MARKIT_PASSWORD = "acuo.markit.password";
    public static final String ACUO_VALUATION_MARKIT_RETRY_DELAY = "acuo.markit.retry.delay";
    public static final String ACUO_VALUATION_MARKIT_CONNECTION_TIMEOUT = "acuo.markit.connection.timeout";

    public static final String ACUO_VALUATION_CLARUS_HOST = "acuo.clarus.host";
    public static final String ACUO_VALUATION_CLARUS_API_KEY = "acuo.clarus.api.key";
    public static final String ACUO_VALUATION_CLARUS_API_SECRET = "acuo.clarus.api.secret";
    public static final String ACUO_VALUATION_CLARUS_CONNECTION_TIMEOUT = "acuo.clarus.connection.timeout";

    public static final String NEO4J_OGM_URL = "neo4j.ogm.url";
    public static final String NEO4J_OGM_USERNAME = "neo4j.ogm.username";
    public static final String NEO4J_OGM_PASSWORD = "neo4j.ogm.password";
    public static final String NEO4J_OGM_DRIVER = "neo4j.ogm.driver";
    public static final String NEO4J_OGM_PACKAGES = "neo4j.ogm.packages";

    private PropertiesHelper(Configuration configuration) {
        super(configuration);
    }

    public static PropertiesHelper of(Configuration configuration) {
        ArgChecker.notNull(configuration, "configuration");
        return new PropertiesHelper(configuration);
    }
}