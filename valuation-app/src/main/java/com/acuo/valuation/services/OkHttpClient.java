package com.acuo.valuation.services;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class OkHttpClient<T extends EndPointConfig> implements ClientEndPoint<T> {

    private static final Logger LOG = LoggerFactory.getLogger(OkHttpClient.class);

    private final T config;
    private final okhttp3.OkHttpClient httpClient;

    @Inject
    public OkHttpClient(okhttp3.OkHttpClient httpClient, T config) {
        this.config = config;
        LOG.debug("OkHttpClient default connection timeout in ms:" + httpClient.connectTimeoutMillis());
        this.httpClient = httpClient.newBuilder().connectTimeout(config.connectionTimeOut(), config.connectionTimeOutUnit()).build();
        LOG.info("Create Markit Http Client with {}", config.toString());
    }

    @Override
    public T config() {
        return config;
    }

    @Override
    public Call call(Request request, Predicate<String> predicate) {
        return new OkHttpCall(this, request, predicate);
    }

    protected String execute(Request request) {
        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Response unsuccesful, unexpected code " + response);
            }
            return response.body().string();
        } catch (IOException ioe) {
            LOG.error("Failed to create {}, the error message {}", request, ioe.getMessage(), ioe);
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
    }

}
