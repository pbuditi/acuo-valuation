package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class MarkitClient extends OkHttpClient<MarkitEndPointConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(MarkitClient.class);

    @Inject
    public MarkitClient(okhttp3.OkHttpClient httpClient, MarkitEndPointConfig config) {
        super(httpClient, config);
    }

    @Override
    public String send(Call call) {
        try {
            String result = null;
            while (result == null) {
                String response = execute(call.getRequest());
                if (call.getPredicate().test(response)) {
                    Thread.sleep(config().getRetryDelayInMilliseconds());
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
}
