package com.acuo.valuation.web.entities;

import lombok.Data;

import java.util.List;

@Data
public class UploadResponse {

    private String txnID;
    private List<Status> statuses;

    public enum StatusType {
        success, failure
    }

    @Data
    public static class Status {
        private final StatusType status;
        private final String remarks;

        public Status(StatusType status, String remarks) {
            this.status = status;
            this.remarks = remarks;
        }
    }
}