package com.acuo.valuation.providers.markit.protocol.reports;

import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.valuation.protocol.reports.Report;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportInput {

    private ReportInput() {
        header = new ReportHeader();
        reports = new ArrayList<>();
    }

    public Report report() {
        Report.ReportBuilder builder = new Report.ReportBuilder(header.name, header.version, header.date);
        reports.stream().filter(r -> r.tradeID != null).forEach(r -> builder.add(r.tradeID, r.type, r.message));
        return builder.build();
    }

    public static ReportInput definition(Report report) {
        ReportInput input = new ReportInput();
        input.header.name = report.name();
        input.header.version = report.version();
        input.header.date = report.valuationDate();
        return input;
    }

    static class ReportHeader {
        @XmlPath("name/text()")
        String name;

        @XmlPath("version/text()")
        String version;

        @XmlPath("date/text()")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        LocalDate date;
    }

    @XmlElement(name = "header", required = true)
    public final ReportHeader header;

    static class Value {

        @XmlPath("tradeid/text()")
        String tradeID;

        @XmlPath("type/text()")
        String type;

        @XmlPath("report/text()")
        String message;
    }

    @XmlElement(name = "report", required = true)
    public final List<Value> reports;
}
