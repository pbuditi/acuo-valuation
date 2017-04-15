package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.EndPointConfig;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
public class DatascopeEndPointConfig implements EndPointConfig {

    private final String scheme;
    private final String host;
    private final int port;
    private final String authpath;
    private final String username;
    private final String password;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;
    private final boolean useProxy;

    @Inject
    public DatascopeEndPointConfig(@Named(ACUO_DATASCOPE_SCHEME) String scheme,
                                   @Named(ACUO_DATASCOPE_HOST) String host,
                                   @Named(ACUO_DATASCOPE_PORT) int port,
                                   @Named(ACUO_DATASCOPE_AUTHPATH) String authpath,
                                   @Named(ACUO_DATASCOPE_USERNAME) String username,
                                   @Named(ACUO_DATASCOPE_PASSWORD) String password,
                                   @Named(ACUO_DATASCOPE_CONNECTION_TIMEOUT) int connectionTimeOut,
                                   @Named(ACUO_DATASCOPE_USE_PROXY) String useProxy)
    {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.authpath = authpath;
        this.username = username;
        this.password = password;
        this.connectionTimeOut = connectionTimeOut;
        this.connectionTimeOutUnit = TimeUnit.MILLISECONDS;
        this.useProxy = Boolean.valueOf(useProxy);
    }

    @Override
    public int connectionTimeOut() {
        return connectionTimeOut;
    }

    @Override
    public TimeUnit connectionTimeOutUnit() {
        return connectionTimeOutUnit;
    }

    @Override
    public boolean useLocalSocksProxy() {
        return useProxy;
    }
}
