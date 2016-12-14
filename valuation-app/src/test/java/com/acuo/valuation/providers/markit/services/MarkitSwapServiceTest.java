package com.acuo.valuation.providers.markit.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.*;

import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.util.ReportHelper;
import com.opengamma.strata.collect.result.Result;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Rule;
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
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, EncryptionModule.class, Neo4jPersistModule.class, EndPointModule.class, ServicesModule.class})
public class MarkitSwapServiceTest {

    @Mock
    Sender sender;

    @Mock
    Retriever retriever;

    @Inject
    Neo4jPersistService session;

    @Inject
    TradeUploadService irsService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    MarkitSwapService service;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        service = new MarkitSwapService(sender,retriever,session);

        irsService.uploadTradesFromExcel(oneIRS.getInputStream());

    }

    @Test
    public void testPriceSwapWithNoErrorReport() {
        when(sender.send(any(List.class))).thenReturn(ReportHelper.reportForSwap("455123"));
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        PricingResults expectedResults = PricingResults.of(Arrays.asList(Result.success(new MarkitValuation(markitValue))));
        when(retriever.retrieve(any(LocalDate.class), any(List.class))).thenReturn(expectedResults);

        PricingResults results = service.getPv("455123");

        assertThat(results).isNotNull().isInstanceOf(PricingResults.class);

        Result<MarkitValuation> swapResult = results.getResults().get(0);
        Condition<MarkitValuation> pvEqualToOne = new Condition<MarkitValuation>(s -> s.getPv().equals(1.0d), "Swap PV not equal to 1.0d");

        assertThat(swapResult.getValue()).is(pvEqualToOne);
    }
}
