package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.EndPointConfig;
import com.acuo.common.util.ArgChecker;
import lombok.Data;
import okhttp3.HttpUrl;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
public class MarkitEndPointConfig implements EndPointConfig {

    private final String scheme;
    private final String host;
    private final int port;
    private final String uploadPath;
    private final String downloadPath;
    private final String username;
    private final String password;
    private final Long retryDelayInMilliseconds;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;

    public MarkitEndPointConfig(HttpUrl httpUrl,
                                String uploadPath,
                                String downloadPath,
                                String username,
                                String password,
                                String retryDelayInMinute,
                                String connectionTimeOutInMilli) {
        this(httpUrl.scheme(), httpUrl.host(), httpUrl.port(), uploadPath, downloadPath, username, password, retryDelayInMinute, connectionTimeOutInMilli, null);
    }

    public MarkitEndPointConfig(String scheme,
                                String host,
                                int port,
                                String uploadPath,
                                String downloadPath,
                                String username,
                                String password,
                                String retryDelayInMinute,
                                String connectionTimeOutInMilli) {
        this(scheme, host, port, uploadPath, downloadPath, username, password, retryDelayInMinute, connectionTimeOutInMilli, null);
    }

    @Inject
    public MarkitEndPointConfig(@Named(ACUO_VALUATION_MARKIT_SCHEME) String scheme,
                                @Named(ACUO_VALUATION_MARKIT_HOST) String host,
                                @Named(ACUO_VALUATION_MARKIT_PORT) int port,
                                @Named(ACUO_VALUATION_MARKIT_UPLOAD_PATH) String uploadPath,
                                @Named(ACUO_VALUATION_MARKIT_DOWNLOAD_PATH) String downloadPath,
                                @Named(ACUO_VALUATION_MARKIT_USERNAME) String username,
                                @Named(ACUO_VALUATION_MARKIT_PASSWORD) String password,
                                @Named(ACUO_VALUATION_MARKIT_RETRY_DELAY) String retryDelayInMinute,
                                @Named(ACUO_VALUATION_MARKIT_CONNECTION_TIMEOUT) String connectionTimeOutInMilli,
                                PBEStringEncryptor encryptor) {
        ArgChecker.notEmpty(scheme, "scheme");
        ArgChecker.notEmpty(host, "host");
        ArgChecker.notEmpty(username, "username");
        ArgChecker.notEmpty(password, "password");
        ArgChecker.notEmpty(password, "retryDelayInMinute");
        ArgChecker.notEmpty(connectionTimeOutInMilli, "connectionTimeOutInMilli");
        this.scheme = scheme;
        this.host = host;
        this.port = (port == 0) ? HttpUrl.defaultPort(scheme) : port;
        this.uploadPath = uploadPath;
        this.downloadPath = downloadPath;
        this.username = username;
        this.password = (encryptor == null) ? password : encryptor.decrypt(password);
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
