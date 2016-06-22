package com.acuo.valuation.markit.services;

import com.acuo.valuation.reports.Report;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.services.Result;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MarkitPricingServiceTest {

    @Mock
    Sender sender;

    @Mock
    Retriever retriever;

    MarkitPricingService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        service = new MarkitPricingService(sender, retriever);

    }

    @Test
    public void testPriceSwapWithErrorReport() {
        when(sender.send(any(SwapDTO.class))).thenReturn(reportError());

        Result result = service.price(swap());

        assertThat(result).isNotNull().isInstanceOf(ErrorResult.class);
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(SwapDTO.class))).thenReturn(report());
        when(retriever.retrieve(any(LocalDate.class), any(String.class))).thenReturn(new SwapResult(1.0d));

        Result result = service.price(swap());

        assertThat(result).isNotNull().isInstanceOf(SwapResult.class);

        SwapResult swapResult = (SwapResult) result;
        Condition<SwapResult> pvEqualToOne = new Condition<SwapResult>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult).is(pvEqualToOne);
    }

    private SwapDTO swap() {
        SwapDTO swapDTO = new SwapDTO();
        swapDTO.setTradeId("trade-id");
        return swapDTO;
    }

    private Report report() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("warning-report","2.2",LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "WARNING", "warning message");
        Report report = reportBuilder.build();
        return report;
    }

    private Report reportError() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("error-report","2.2",LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "ERROR", "error message");
        Report report = reportBuilder.build();
        return report;
    }

}
