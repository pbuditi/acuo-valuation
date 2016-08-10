package com.acuo.valuation.providers.markit.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.requests.RequestParser;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.services.ClientEndPoint;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({EndPointModule.class, ServicesModule.class, JaxbModule.class})
public class PortfolioValuationsSenderIntegrationTest {

    @Rule
    public ResourceFile res = new ResourceFile("/markit/markit-sample.xml");

    @Inject
    ClientEndPoint clientEndPoint;

    @Inject
    RequestParser requestParser;

    @Inject
    ReportParser parser;

    @Test
    public void testResourceFileExist() throws Exception {
        assertTrue(res.getContent().length() > 0);
        // assertTrue(res.getFile().exists());
    }

    @Test
    public void testUploadFile() throws Exception {
        PortfolioValuationsSender mps = new PortfolioValuationsSender(clientEndPoint, requestParser, parser);

        Report report = mps.send(res.getContent());

        assertThat(report).isNotNull();
    }
}
