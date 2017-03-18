package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class MarkitClient extends OkHttpClient<MarkitEndPointConfig> {

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
                if (log.isTraceEnabled()) {
                    log.trace(response);
                }
                if (call.getPredicate().test(response)) {
                    Thread.sleep(config().getRetryDelayInMilliseconds());
                } else {
                    result = response;
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to create the request, the error message {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
