package com.acuo.valuation.services.markit;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.acuo.common.util.ResourceFile;

@Ignore
public class PortfolioValuationsSenderTest {

	@Rule
	public ResourceFile res = new ResourceFile("/test-markit-upload.xml");

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***");

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		assertTrue(res.getFile().exists());
	}

	@Test
	public void testUploadFile() throws Exception {
		PortfolioValuationsSender mps = new PortfolioValuationsSender(markitEndPointConfig);
		String key = mps.uploadFile(res.getFile());
		mps.fetchUploadReport(key);
	}
}
