package com.acuo.valuation.providers.clarus.services;

import com.acuo.valuation.services.Call;
import com.acuo.valuation.services.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ClarusClient extends OkHttpClient<ClarusEndPointConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(ClarusClient.class);

    @Inject
    public ClarusClient(okhttp3.OkHttpClient httpClient, ClarusEndPointConfig config) {
        super(httpClient, config);
    }

    @Override
    public String send(Call call) {
        try {
            String result = null;
            while (result == null) {
                String response = execute(call.getRequest());
                if (call.getPredicate().test(response)) {
                    result = "an error occurred";
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
}
