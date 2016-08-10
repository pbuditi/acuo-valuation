package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.providers.markit.product.swap.IrSwap;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.protocol.results.ErrorResult;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.protocol.results.SwapResult;
import com.acuo.valuation.util.ReportHelper;
import com.acuo.valuation.util.SwapHelper;
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
        when(sender.send(any(IrSwap.class))).thenReturn(ReportHelper.reportError());

        Result result = service.price(SwapHelper.swap());

        assertThat(result).isNotNull().isInstanceOf(ErrorResult.class);
    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(IrSwap.class))).thenReturn(ReportHelper.report());
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        when(retriever.retrieve(any(LocalDate.class), any(String.class))).thenReturn(new SwapResult(markitValue));

        Result result = service.price(SwapHelper.swap());

        assertThat(result).isNotNull().isInstanceOf(SwapResult.class);

        SwapResult swapResult = (SwapResult) result;
        Condition<SwapResult> pvEqualToOne = new Condition<SwapResult>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult).is(pvEqualToOne);
    }
}
