package com.acuo.valuation.providers.clarus.protocol;

import com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginMethodology;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class RequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RequestBuilder.class);

    private final ObjectMapper objectMapper;

    private String data;
    private DataFormat format;
    private DataType type;

    private LocalDate valueDate = LocalDate.now();
    private MarginMethodology marginMethodology;

    private RequestBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static RequestBuilder create(ObjectMapper objectMapper) {
        return new RequestBuilder(objectMapper);
    }

    public RequestBuilder addData(String data) {
        this.data = data;
        return this;
    }

    public RequestBuilder addType(DataType type) {
        this.type = type;
        return this;
    }

    public RequestBuilder addFormat(DataFormat format) {
        this.format = format;
        return this;
    }

    public RequestBuilder marginMethodology(MarginMethodology marginMethodology) {
        this.marginMethodology = marginMethodology;
        return this;
    }

    public RequestBuilder valueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    public String build() {
        PortfolioData portfolioData = PortfolioDataBuilder
                .create()
                .addData(data)
                .addFormat(format)
                .addType(type)
                .build();
        EnvelopeBuilder envelopeBuilder = EnvelopeBuilder
                .create(objectMapper)
                .marginMethodology(MarginMethodology.CME)
                .portfolioData(portfolioData);
        try {
            String json = envelopeBuilder.asJson();
            LOG.debug("request: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            LOG.error("error building request", e);
            throw new RuntimeException(e);
        }
    }

}
