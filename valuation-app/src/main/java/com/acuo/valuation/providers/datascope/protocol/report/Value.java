package com.acuo.valuation.providers.datascope.protocol.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Value {

    @JsonProperty("ExtractedFileId")
    private String extractedfileid;
    @JsonProperty("ReportExtractionId")
    private String reportextractionid;
    @JsonProperty("ScheduleId")
    private String scheduleid;
    @JsonProperty("FileType")
    private String filetype;
    @JsonProperty("ExtractedFileName")
    private String extractedfilename;

    @JsonProperty("ContentsExists")
    private boolean contentsexists;
    @JsonProperty("Size")
    private int size;

}
