package com.acuo.valuation.modules;

import com.acuo.valuation.PropertiesHelper;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.EndPointConfig;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class EndPointConfigProvider implements Provider<EndPointConfig> {

    @Inject
    PBEStringEncryptor encryptor;

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
                markitUsername, encryptor.decrypt(markitPassword), Long.parseLong(markitRetryDelay));
        return config;
    }
}