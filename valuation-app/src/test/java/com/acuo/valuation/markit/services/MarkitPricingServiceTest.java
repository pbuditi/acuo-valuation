package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.responses.MarkitValue;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.results.ErrorResult;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.results.SwapResult;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(sender.send(any(IrSwap.class))).thenReturn(reportError());

        Result result = service.price(swap());

        assertThat(result).isNotNull().isInstanceOf(ErrorResult.class);
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(IrSwap.class))).thenReturn(report());
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        when(retriever.retrieve(any(LocalDate.class), any(String.class))).thenReturn(new SwapResult(markitValue));

        Result result = service.price(swap());

        assertThat(result).isNotNull().isInstanceOf(SwapResult.class);

        SwapResult swapResult = (SwapResult) result;
        Condition<SwapResult> pvEqualToOne = new Condition<SwapResult>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult).is(pvEqualToOne);
    }

    private IrSwap swap() {
        IrSwap swapDTO = new IrSwap();
        swapDTO.setTradeId("trade-id");
        return swapDTO;
    }

    private Report report() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("warning-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "WARNING", "warning message");
        Report report = reportBuilder.build();
        return report;
    }

    private Report reportError() {
        Report.ReportBuilder reportBuilder = new Report.ReportBuilder("error-report", "2.2", LocalDate.of(2016, 6, 10));
        reportBuilder.add("trade-id", "ERROR", "error message");
        Report report = reportBuilder.build();
        return report;
    }

}
