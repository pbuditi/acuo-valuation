package com.acuo.valuation.modules;

import com.acuo.valuation.PropertiesHelper;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.EndPointConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class EndPointConfigProvider implements Provider<EndPointConfig> {

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
    private String markitRetryDelay;

    @Override
    public EndPointConfig get() {
        MarkitEndPointConfig config = new MarkitEndPointConfig(markitHost,
                markitUsername, markitPassword, Long.parseLong(markitRetryDelay));
        return config;
    }
}