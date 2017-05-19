package com.acuo.valuation.providers.datascope.service.intraday;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@Data
@JsonTypeInfo(include= As.WRAPPER_OBJECT, use= Id.NAME)
@JsonPropertyOrder({ "@odata.type", "ContentFieldNames", "IdentifierList", "Condition" })
class ExtractionRequest {

    @JsonProperty("@odata.type")
    private String dataType = "#ThomsonReuters.Dss.Api.Extractions.ExtractionRequests.IntradayPricingExtractionRequest";

    @JsonProperty("ContentFieldNames")
    private List<String>  contentFieldNames = ImmutableList.of("RIC","Mid Price","Last Update Time");

    @JsonProperty("IdentifierList")
    private IdentifierList identifierList = new IdentifierList();

    @JsonProperty("Condition")
    private Condition condition = new Condition();

    @Data
    private class Condition {
        @JsonProperty("ScalableCurrency")
        private boolean scalableCurrency = true;

    }

    @Data
    private class IdentifierList {

        @JsonProperty("@odata.type")
        private String dataType = "#ThomsonReuters.Dss.Api.Extractions.ExtractionRequests.InstrumentIdentifierList";

        @JsonProperty("InstrumentIdentifiers")
        private Map<String, String> instrumentIdentifiers = ImmutableMap.of("Identifier", "USDEUR=R", "IdentifierType","Ric");

        @JsonProperty("UseUserPreferencesForValidationOptions")
        private boolean useUserPreferencesForValidationOptions =  false;

        @JsonProperty("ValidationOptions")
        private String ValidationOptions = null;
    }

}
