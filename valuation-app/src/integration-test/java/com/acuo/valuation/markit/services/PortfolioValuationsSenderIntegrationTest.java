package com.acuo.valuation.markit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.services.ClientEndPoint;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.reports.ReportParser;

import javax.inject.Inject;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ServicesModule.class, JaxbModule.class })
public class PortfolioValuationsSenderIntegrationTest {

	@Rule
	public ResourceFile res = new ResourceFile("/it-requests/markit-sample.xml");

	@Rule
	public ResourceFile test = new ResourceFile("/it-requests/test.xml");

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
