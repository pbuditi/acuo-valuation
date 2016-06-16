package com.acuo.valuation.services.markit;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.acuo.common.util.ResourceFile;

@Ignore
public class PortfolioValuationsSenderTest {

	@Rule
	public ResourceFile res = new ResourceFile("/it-requests/markit-sample.xml");

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***");

	@Test
	@Ignore
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
	}

	@Test
	@Ignore
	public void testUploadFile() throws Exception {
		PortfolioValuationsSender mps = new PortfolioValuationsSender(markitEndPointConfig);
		String key = mps.uploadFile(res.getFile());
		mps.fetchUploadReport(key);
	}
}
