package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.http.client.EndPointConfig;
import lombok.Data;
import lombok.ToString;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
@ToString(exclude = "apikey")
public class ReutersEndPointConfig implements EndPointConfig {

    private final String scheme;
    private final String host;
    private final int port;
    private final String uploadPath;
    private final String apikey;
    private final String headerPosition;
    private final String headerApplicationId;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;
    private final boolean useProxy;

    @Inject
    public ReutersEndPointConfig(@Named(ACUO_VALUATION_REUTERS_SCHEME) String scheme,
                                 @Named(ACUO_VALUATION_REUTERS_HOST) String host,
                                 @Named(ACUO_VALUATION_REUTERS_PORT) int port,
                                 @Named(ACUO_VALUATION_REUTERS_UPLOAD_PATH) String uploadPath,
                                 @Named(ACUO_VALUATION_REUTERS_API_KEY) String apikey,
                                 @Named(ACUO_VALUATION_REUTERS_HEADER_POSITION) String headerPosition,
                                 @Named(ACUO_VALUATION_REUTERS_HEADER_APPLICATION_ID) String headerApplicationId,
                                 @Named(ACUO_VALUATION_REUTERS_CONNECTION_TIMEOUT) int connectionTimeOut,
                                 @Named(ACUO_VALUATION_REUTERS_USE_PROXY) String useProxy,
                                 PBEStringEncryptor encryptor) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.uploadPath = uploadPath;
        this.apikey = (encryptor == null) ? apikey : encryptor.decrypt(apikey);
        this.headerPosition = headerPosition;
        this.headerApplicationId = headerApplicationId;
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
