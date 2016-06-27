package com.acuo.valuation.services;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Predicate;

public final class OkHttpClient implements ClientEndPoint {

    private static final Logger LOG = LoggerFactory.getLogger(OkHttpClient.class);

    private final EndPointConfig config;
    private final okhttp3.OkHttpClient httpClient;

    @Inject
    public OkHttpClient(okhttp3.OkHttpClient httpClient, EndPointConfig config) {
        this.config = config;
        this.httpClient = httpClient;
        LOG.info("Create Markit Http Client with {}", config.toString());
    }

    @Override
    public EndPointConfig config() {
        return config;
    }

    @Override
    public ClientCall call(Request request, Predicate<String> predicate) {
        return new OkHttpClientCall(this, request, predicate);
    }

    public String send(ClientCall call) {
        try {
            String result = null;
            while (result == null) {
                String response = execute(call.getRequest());
                if (call.getPredicate().test(response)) {
                    Thread.sleep(config.retryDelayInMilliseconds());
                } else {
                    result = response;
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error("Failed to create the request, the error message {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String execute(Request request) {
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
