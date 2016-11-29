package com.acuo.valuation.providers.markit.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.Neo4jPersistModule;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.modules.Neo4jIntegrationTestModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.util.ReportHelper;
import com.opengamma.strata.collect.result.Result;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, Neo4jPersistModule.class, Neo4jIntegrationTestModule.class})
public class MarkitSwapServiceTest {

    @Mock
    Sender sender;

    @Mock
    Retriever retriever;

    @Inject
    Neo4jPersistService session;

    MarkitSwapService service;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        service = new MarkitSwapService(sender,retriever,session);

    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
;
        when(sender.send(any(List.class))).thenReturn(ReportHelper.reportForSwap());
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.getPv("irsvt1");

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }
}
