package com.acuo.valuation;

import static com.acuo.valuation.PropertiesHelper.ACUO_VALUATION_APP_CTX_PATH;
import static com.acuo.valuation.PropertiesHelper.ACUO_VALUATION_APP_HOST;
import static com.acuo.valuation.PropertiesHelper.ACUO_VALUATION_APP_PORT;
import static com.acuo.valuation.PropertiesHelper.ACUO_VALUATION_APP_REST_MAPPING_PREFIX;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.app.ResteasyConfig;
import com.acuo.common.http.server.HttpServerConnectorConfig;
import com.acuo.common.http.server.HttpServerWrapperConfig;

public class ResteasyConfigImpl implements ResteasyConfig {

	private static final Logger LOG = LoggerFactory.getLogger(ResteasyConfigImpl.class);

	@Inject
	@Named(ACUO_VALUATION_APP_PORT)
	public Integer port;

	@Inject
	@Named(ACUO_VALUATION_APP_HOST)
	public String ipAddress;

	@Inject
	@Named(ACUO_VALUATION_APP_CTX_PATH)
	public String contextPath;

	@Inject
	@Named(ACUO_VALUATION_APP_REST_MAPPING_PREFIX)
	public String mappingPrefix;

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
