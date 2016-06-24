package com.acuo.valuation.markit.services;

import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.EndPointConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

public class MarkitClient implements ClientEndPoint {

    private static final Logger LOG = LoggerFactory.getLogger(MarkitClient.class);

    private final EndPointConfig config;
    private final OkHttpClient httpClient;

    @Inject
    public MarkitClient(OkHttpClient httpClient, EndPointConfig config) {
        this.config = config;
        this.httpClient = httpClient;
        LOG.info("Create Markit Http Client with {}", config.toString());
    }

    public MarkitGetRequestBuilder get() {
        return new MarkitGetRequestBuilder(this, config);
    }

    public MarkitPostRequestBuilder post() {
        return new MarkitPostRequestBuilder(this, config);
    }

    public String send(MarkitClientCall call) {
        try {
            String result = null;
            while (result == null) {
                String response = execute(call.getRequest());
                if(call.getPredicate().test(response)) {
                    Thread.sleep(config.retryDelayInMilliseconds());
                } else {
                    result = response;
                }
            }
            return result;
        } catch (Exception e) {
            LOG.error("Failed to send the request, the error message {}", e.getMessage(), e);
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
            LOG.error("Failed to send {}, the error message {}", request, ioe.getMessage(), ioe);
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
    }





}
