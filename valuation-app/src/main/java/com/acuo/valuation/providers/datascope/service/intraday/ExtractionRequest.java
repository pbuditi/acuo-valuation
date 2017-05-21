package com.acuo.valuation.providers.datascope.service.intraday;

import com.acuo.valuation.quartz.FXScheduledValueJob;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import static java.util.stream.Collectors.toList;

@Data
@JsonTypeInfo(include= As.WRAPPER_OBJECT, use= Id.NAME)
@JsonPropertyOrder({ "@odata.type", "ContentFieldNames", "IdentifierList", "Condition" })
@Slf4j
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
        private List<InstrumentIdentifier> instrumentIdentifiers = rics();

        @JsonProperty("UseUserPreferencesForValidationOptions")
        private boolean useUserPreferencesForValidationOptions =  false;

        @JsonProperty("ValidationOptions")
        private String ValidationOptions = null;
    }


    @Data
    class InstrumentIdentifier {

        InstrumentIdentifier() {

        }

        @JsonProperty("Identifier")
        private String identifier;

        @JsonProperty("IdentifierType")
        private String type = "Ric";
    }

    private List<InstrumentIdentifier> rics() {
        String file = readFile("/fx/RIC.csv");
        try (BufferedReader reader = new BufferedReader(new StringReader(file))) {
            return reader.lines()
                    .collect(toList())
                    .stream()
                    .map(ric -> {
                        InstrumentIdentifier instrument = new InstrumentIdentifier();
                        instrument.setIdentifier(ric);
                        return instrument;
                    })
                    .collect(toList());
        } catch (Exception e) {
            log.error("error in getFx :" + e);
            return Collections.emptyList();
        }
    }

    private static String readFile(String filePath) {
        try {
            return IOUtils.toString(FXScheduledValueJob.class.getResourceAsStream(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

}
