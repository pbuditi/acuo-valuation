package com.acuo.valuation.markit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.reports.Report;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.reports.ReportParser;

@Ignore
public class PortfolioValuationsSenderIntegrationTest {

	@Rule
	public ResourceFile res = new ResourceFile("/it-requests/markit-sample.xml");

	@Rule
	public ResourceFile test = new ResourceFile("/it-requests/test.xml");

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***", 1l);

	@Mock
	RequestParser requestParser;

	@Mock
	ReportParser parser;

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		// assertTrue(res.getFile().exists());
	}

	@Test
	public void testUploadFile() throws Exception {
		PortfolioValuationsSender mps = new PortfolioValuationsSender(markitEndPointConfig, requestParser, parser);

		Report report = mps.send(res.getContent());

		assertThat(report).isNotNull();
	}
}
