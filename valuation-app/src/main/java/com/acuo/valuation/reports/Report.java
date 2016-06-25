package com.acuo.valuation.reports;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Report {

    private final String name;
    private final String version;
    private final LocalDate valuationDate;
    private final Map<String, List<Item>> itemsPerTradeId;

    private Report(ReportBuilder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.valuationDate = builder.valuationDate;
        this.itemsPerTradeId = builder.itemsPerTradeId;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public LocalDate valuationDate() {
        return valuationDate;
    }

    public Map<String, List<Item>> itemsPerTradeId() {
        return itemsPerTradeId;
    }

    @Data
    public static class Item {
        private final String type;
        private final String message;
    }

    public static class ReportBuilder {

        private final String name;
        private final String version;
        private final LocalDate valuationDate;
        private Map<String, List<Item>> itemsPerTradeId = new HashMap<>();

        public ReportBuilder(String name, String version, LocalDate valuationDate) {
            this.name = name;
            this.version = version;
            this.valuationDate = valuationDate;
        }

        public ReportBuilder add(String tradeId, String type, String message) {
            itemsPerTradeId.computeIfAbsent(tradeId, k -> new LinkedList<Item>()).add(new Item(type, message));
            return this;
        }

        public Report build() {
            return new Report(this);
        }
    }

}
