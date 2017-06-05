package com.acuo.valuation.providers.datascope.service.intraday;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExtractionResponse {

    public ExtractionResponse() {

    }
    @JsonProperty("Contents")
    private List<Content> contents;

    @Data
    static class Content {

        public Content() {

        }

        @JsonProperty("Last Update Time")
        @JsonFormat(pattern="MM/dd/yyyy HH:mm:ss")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime lastUpdatedTime;

        @JsonProperty("Identifier")
        private String identifier;

        @JsonProperty("Mid Price")
        private Double midPrice;

        @JsonProperty("IdentifierType")
        private String identifierType;

        @JsonProperty("RIC")
        private String ricCode;
    }
}
