package com.acuo.valuation;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.http.server.HttpServerConnectorConfig;
import com.acuo.common.http.server.HttpServerWrapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static com.acuo.valuation.utils.PropertiesHelper.*;

public class ResteasyConfigImpl implements ResteasyConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ResteasyConfigImpl.class);

    private final Integer port;
    private final String ipAddress;
    private final String contextPath;
    private final String mappingPrefix;

    @Inject
    public ResteasyConfigImpl(@Named(ACUO_VALUATION_APP_PORT) Integer port,
                              @Named(ACUO_VALUATION_APP_HOST) String ipAddress,
                              @Named(ACUO_VALUATION_APP_CTX_PATH) String contextPath,
                              @Named(ACUO_VALUATION_APP_REST_MAPPING_PREFIX) String mappingPrefix) {
        this.port = port;
        this.ipAddress = ipAddress;
        this.contextPath = contextPath;
        this.mappingPrefix = mappingPrefix;
    }

    @Override
    public HttpServerWrapperConfig getConfig() {
        LOG.info("building an http server config with context [{}], ip [{}], port [{}], mapping prefix [{}]",
                contextPath, ipAddress, port, mappingPrefix);

        HttpServerWrapperConfig config = new HttpServerWrapperConfig()
                .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp(ipAddress, port));

        config.setContextPath(formatContextPath(contextPath));

        config.addInitParameter("resteasy.servlet.mapping.prefix", mappingPrefix);

        return config;
    }

    private static String formatContextPath(String contextPath) {
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

}
