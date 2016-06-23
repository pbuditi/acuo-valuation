package com.acuo.valuation.markit.services;

import com.acuo.valuation.utils.LoggingInterceptor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

public class MarkitClient {

    private static final Logger LOG = LoggerFactory.getLogger(MarkitClient.class);

    private final MarkitEndPointConfig markitEndPointConfig;
    private final OkHttpClient httpClient;

    public MarkitClient(OkHttpClient httpClient, MarkitEndPointConfig markitEndPointConfig) {
        this.markitEndPointConfig = markitEndPointConfig;
        this.httpClient = httpClient; //new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        LOG.info("Create Markit Http Client with {}", markitEndPointConfig.toString());
    }

    public RequestBuilder request() {
        return new RequestBuilder(markitEndPointConfig);
    }

    private String send(Request request) {
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

    public class RequestBuilder {

        private final MarkitEndPointConfig markitEndPointConfig;
        private Predicate<String> predicate = s -> false;

        public RequestBuilder(MarkitEndPointConfig markitEndPointConfig) {
            this.markitEndPointConfig = markitEndPointConfig;
        }

        private RequestBody body;

        public RequestBuilder with(File file) {
            body = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), file);
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("username", markitEndPointConfig.username())
                    .addFormDataPart("password", markitEndPointConfig.password())
                    .addPart(MultipartBody.Part.createFormData("theFile", "theFile", body)).build();
            return this;
        }

        public RequestBuilder withKey(String value) {
            body = new FormBody.Builder()
                    .add("username", markitEndPointConfig.username())
                    .add("password", markitEndPointConfig.password())
                    .add("key", value)
                    .add("version", "2").build();
            return this;
        }

        public RequestBuilder withAsOfDate(String value) {
            body = new FormBody.Builder()
                    .add("username", markitEndPointConfig.username())
                    .add("password", markitEndPointConfig.password())
                    .add("asOf", value)
                    .add("format", "xml").build();
            return this;
        }

        public RequestBuilder retryUntil(Predicate<String> predicate) {
            this.predicate = predicate;
            return this;
        }

        public String send() {
            try {
                String result = null;
                while (result == null) {
                    Request request = new Request.Builder().url(markitEndPointConfig.url()).post(body).build();
                    String response = MarkitClient.this.send(request);
                    if(predicate.test(response)) {
                        Thread.sleep(markitEndPointConfig.retryDelayInMilliseconds());
                    } else {
                        result = response;
                    }
                }
                return result;
            } catch (Exception e) {
                LOG.error("Failed to send {}, the error message {}", body, e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
