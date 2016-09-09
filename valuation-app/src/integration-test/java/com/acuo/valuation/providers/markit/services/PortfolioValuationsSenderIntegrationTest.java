package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.reports.Report;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({EndPointModule.class, ServicesModule.class, MappingModule.class})
public class PortfolioValuationsSenderIntegrationTest {

    @Rule
    public ResourceFile res = new ResourceFile("/markit/markit-sample.xml");

    @Inject
    ClientEndPoint clientEndPoint;

    @Inject
    @Named("markit")
    Transformer<SwapTrade> transformer;

    @Inject
    ReportParser parser;

    @Test
    public void testResourceFileExist() throws Exception {
        assertTrue(res.getContent().length() > 0);
        assertTrue(res.getFile().exists());
    }

    @Test
    public void testUploadFile() throws Exception {
        PortfolioValuationsSender mps = new PortfolioValuationsSender(clientEndPoint, parser, transformer);

        Report report = mps.send(res.getContent());

        assertThat(report).isNotNull();
    }
}
