package com.acuo.valuation.providers.markit.services;

import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.services.EndPointConfig;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
public class MarkitEndPointConfig implements EndPointConfig {

    private final String url;
    private final String username;
    private final String password;
    private final Long retryDelayInMilliseconds;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;

    //@Inject
    //PBEStringEncryptor encryptor;

    @Inject
    public MarkitEndPointConfig(@Named(ACUO_VALUATION_MARKIT_HOST) String url,
                                @Named(ACUO_VALUATION_MARKIT_USERNAME) String username,
                                @Named(ACUO_VALUATION_MARKIT_PASSWORD) String password,
                                @Named(ACUO_VALUATION_MARKIT_RETRY_DELAY) String retryDelayInMinute,
                                @Named(ACUO_VALUATION_MARKIT_CONNECTION_TIMEOUT) String connectionTimeOutInMilli) {
        ArgChecker.notEmpty(url, "url");
        ArgChecker.notEmpty(username, "username");
        ArgChecker.notEmpty(password, "password");
        ArgChecker.notEmpty(password, "retryDelayInMinute");
        this.url = url;
        this.username = username;
        this.password = password;//encryptor.decrypt(password);
        this.retryDelayInMilliseconds = TimeUnit.MINUTES.toMillis(Long.parseLong(retryDelayInMinute));
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
