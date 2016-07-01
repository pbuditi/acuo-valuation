package com.acuo.valuation.util;

import com.acuo.valuation.reports.Report;

import java.time.LocalDate;

public class ReportHelper {

    private ReportHelper() {}

    public static Report report() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("warning-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "WARNING", "warning message");
        Report report = reportBuilder.build();
        return report;
    }

    public static Report reportError() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("error-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "ERROR", "error message");
        Report report = reportBuilder.build();
        return report;
    }
}
