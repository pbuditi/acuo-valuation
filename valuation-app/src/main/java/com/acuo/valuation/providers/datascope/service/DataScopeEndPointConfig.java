package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.EndPointConfig;
import lombok.Data;
import lombok.ToString;
import okhttp3.HttpUrl;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static com.acuo.valuation.utils.PropertiesHelper.*;

@Data
@ToString(exclude = "password")
@Singleton
public class DataScopeEndPointConfig implements EndPointConfig {

    private final String scheme;
    private final String host;
    private final int port;
    private final String authpath;
    private final String schedulepath;
    private final String username;
    private final String password;
    private final int connectionTimeOut;
    private final TimeUnit connectionTimeOutUnit;
    private final boolean useProxy;
    private final String listIdFX;
    private final String reportTemplateIdFX;
    private final String statuspath;
    private final String reportpath;
    private final String downloadpath;
    private final String intradaypath;
    private final Long retryDelayInMilliseconds;

    private final String listIdBond;
    private final String reportTemplateIdBond;

    public DataScopeEndPointConfig(HttpUrl httpUrl,
                                   String authpath,
                                   String schedulepath,
                                   String username,
                                   String password,
                                   int connectionTimeOut,
                                   String useProxy,
                                   String listIdFX,
                                   String reportTemplateIdFX,
                                   String statuspath,
                                   String reportpath,
                                   String downloadpath,
                                   String intradaypath,
                                   String listIdBond,
                                   String reportTemplateIdBond,
                                   String retryDelayInSeconds,
                                   PBEStringEncryptor encryptor) {
        this(
                httpUrl.scheme(),
                httpUrl.host(),
                httpUrl.port(),
                authpath,
                schedulepath,
                username,
                password,
                connectionTimeOut,
                useProxy,
                listIdFX,
                reportTemplateIdFX,
                statuspath,
                reportpath,
                downloadpath,
                intradaypath,
                listIdBond,
                reportTemplateIdBond,
                retryDelayInSeconds,
                encryptor);
    }

    @Inject
    public DataScopeEndPointConfig(@Named(ACUO_DATASCOPE_SCHEME) String scheme,
                                   @Named(ACUO_DATASCOPE_HOST) String host,
                                   @Named(ACUO_DATASCOPE_PORT) int port,
                                   @Named(ACUO_DATASCOPE_AUTHPATH) String authpath,
                                   @Named(ACUO_DATASCOPE_SCHEDULEPATH) String schedulepath,
                                   @Named(ACUO_DATASCOPE_USERNAME) String username,
                                   @Named(ACUO_DATASCOPE_PASSWORD) String password,
                                   @Named(ACUO_DATASCOPE_CONNECTION_TIMEOUT) int connectionTimeOut,
                                   @Named(ACUO_DATASCOPE_USE_PROXY) String useProxy,
                                   @Named(ACUO_DATASCOPE_LIST_ID_FX) String listIdFX,
                                   @Named(ACUO_DATASCOPE_REPORT_TEMPLATE_ID_FX) String reportTemplateIdFX,
                                   @Named(ACUO_DATASCOPE_STATUSPATH) String statuspath,
                                   @Named(ACUO_DATASCOPE_REPORTPATH) String reportpath,
                                   @Named(ACUO_DATASCOPE_DOWNLOADPATH) String downloadpath,
                                   @Named(ACUO_DATASCOPE_INTRADAYPATH) String intradaypath,
                                   @Named(ACUO_DATASCOPE_LIST_ID_BOND) String listIdBond,
                                   @Named(ACUO_DATASCOPE_REPORT_TEMPLATE_ID_BOND) String reportTemplateIdBond,
                                   @Named(ACUO_DATASCOPE_RETRY_DELAY) String retryDelayInSeconds,
                                   PBEStringEncryptor encryptor) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.authpath = authpath;
        this.schedulepath = schedulepath;
        this.username = username;
        this.password = (encryptor == null) ? password : encryptor.decrypt(password);
        this.connectionTimeOut = connectionTimeOut;
        this.connectionTimeOutUnit = TimeUnit.MILLISECONDS;
        this.useProxy = Boolean.valueOf(useProxy);
        this.listIdFX = listIdFX;
        this.reportTemplateIdFX = reportTemplateIdFX;
        this.statuspath = statuspath;
        this.reportpath = reportpath;
        this.downloadpath = downloadpath;
        this.intradaypath = intradaypath;
        this.listIdBond = listIdBond;
        this.reportTemplateIdBond = reportTemplateIdBond;
        this.retryDelayInMilliseconds = TimeUnit.SECONDS.toMillis(Long.parseLong(retryDelayInSeconds));
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