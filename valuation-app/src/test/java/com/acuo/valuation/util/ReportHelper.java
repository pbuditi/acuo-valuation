package com.acuo.valuation.util;

import com.acuo.valuation.protocol.reports.Report;

import java.time.LocalDate;

public class ReportHelper {

    private ReportHelper() {}

    public static Report report() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("warning-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("cid1", "WARNING", "warning message");
        Report report = reportBuilder.build();
        return report;
    }

    public static Report reportError() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("error-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("cid1", "ERROR", "error message");
        Report report = reportBuilder.build();
        return report;
    }

    public static Report reportForSwap(String tradeId) {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("warning-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add(tradeId, "WARNING", "warning message");
        Report report = reportBuilder.build();
        return report;
    }

}
