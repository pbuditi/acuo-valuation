package com.acuo.valuation.markit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.reports.ReportParser;

public class PortfolioValuationsSenderTest {

	@Rule
	public ResourceFile res = new ResourceFile("/it-requests/markit-sample.xml");

	@Rule
	public ResourceFile test = new ResourceFile("/it-requests/test.xml");

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***");

	@Mock
	ReportParser parser;

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		// assertTrue(res.getFile().exists());
	}

	@Test
	public void testUploadFile() throws Exception {
		PortfolioValuationsSender mps = new PortfolioValuationsSender(markitEndPointConfig, parser);
		String key = mps.uploadFile(res.getFile());
		String report = mps.fetchUploadReport(key);

		System.out.println(report);

		assertThat(report).isNotNull();
	}
}
