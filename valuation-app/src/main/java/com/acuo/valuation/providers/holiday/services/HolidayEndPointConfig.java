package com.acuo.valuation.providers.holiday.services;

import com.acuo.common.http.client.EndPointConfig;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
public class HolidayEndPointConfig implements EndPointConfig {

    private final String scheme;
    private final String host;
    private final int port;
    private final String path;
    private final String apikey;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;
    private final boolean useProxy;

    @Inject
    public HolidayEndPointConfig(@Named(ACUO_HOLIDAY_SCHEME) String scheme,
                                 @Named(ACUO_HOLIDAY_HOST) String host,
                                 @Named(ACUO_HOLIDAY_PORT) int port,
                                 @Named(ACUO_HOLIDAY_PATH) String path,
                                 @Named(ACUO_HOLIDAY_API_KEY) String apikey,
                                 @Named(ACUO_HOLIDAY_CONNECTION_TIMEOUT) int connectionTimeOut)
    {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.apikey = apikey;
        this.connectionTimeOut = connectionTimeOut;
        this.connectionTimeOutUnit = TimeUnit.MILLISECONDS;
        this.useProxy = false;
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
