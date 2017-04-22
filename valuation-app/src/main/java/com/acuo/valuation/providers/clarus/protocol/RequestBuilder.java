package com.acuo.valuation.providers.clarus.protocol;

import com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class RequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RequestBuilder.class);

    private final ObjectMapper objectMapper;
    private LocalDate valueDate = LocalDate.now();
    private String portfolios;
    private String whatIfs;
    private DataModel model;

    private RequestBuilder(ObjectMapper objectMapper, String portfolios) {
        this.objectMapper = objectMapper;
        this.portfolios = portfolios;
    }

    public static RequestBuilder create(ObjectMapper objectMapper, String portfolios) {
        return new RequestBuilder(objectMapper, portfolios);
    }

    public RequestBuilder addDataModel(DataModel dataModel) {
        this.model = dataModel;
        return this;
    }

    public RequestBuilder addWhatIfs(String whatIfs) {
        this.whatIfs = whatIfs;
        return this;
    }

    public String build() {
        try {
            String json = objectMapper.writeValueAsString(new Request(
                    this.valueDate,
                    this.model,
                    this.portfolios,
                    this.whatIfs
            ));
            LOG.debug("request: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            LOG.error("error building request", e);
            throw new RuntimeException(e);
        }
    }

}
