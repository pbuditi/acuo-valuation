package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.http.client.EndPointConfig;
import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.utils.PropertiesHelper;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

@Data
public class ClarusEndPointConfig implements EndPointConfig {

    private final String host;
    private final String key;
    private final String secret;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;

    @Inject
    public ClarusEndPointConfig(@Named(PropertiesHelper.ACUO_VALUATION_CLARUS_HOST) String host,
                                @Named(PropertiesHelper.ACUO_VALUATION_CLARUS_API_KEY) String key,
                                @Named(PropertiesHelper.ACUO_VALUATION_CLARUS_API_SECRET) String secret,
                                @Named(PropertiesHelper.ACUO_VALUATION_CLARUS_CONNECTION_TIMEOUT) String connectionTimeOutInMilli) {
        ArgChecker.notEmpty(host, "host");
        ArgChecker.notEmpty(key, "key");
        ArgChecker.notEmpty(secret, "secret");
        ArgChecker.notEmpty(connectionTimeOutInMilli, "connectionTimeOutInMilli");
        this.host = host;
        this.key = key;
        this.secret = secret;
        this.connectionTimeOut = Integer.valueOf(connectionTimeOutInMilli);
        this.connectionTimeOutUnit = TimeUnit.MILLISECONDS;
    }

    @Override
    public int connectionTimeOut() {
        return connectionTimeOut;
    }

    @Override
    public TimeUnit connectionTimeOutUnit() {
        return connectionTimeOutUnit;
    }
}
