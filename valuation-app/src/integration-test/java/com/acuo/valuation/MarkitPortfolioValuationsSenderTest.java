package com.acuo.valuation;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.services.MarkitPortfolioValuationsSender;

@Ignore
public class MarkitPortfolioValuationsSenderTest {

	@Rule
	public ResourceFile res = new ResourceFile("/test-markit-upload.xml");

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		assertTrue(res.getFile().exists());
	}

	@Test
	public void testUploadFile() throws Exception {
		MarkitPortfolioValuationsSender mps = new MarkitPortfolioValuationsSender("https://pv.markit.com/upload",
				"acuosamedayupload", "***REMOVED***");
		String key = mps.uploadFile(res.getFile());
		mps.fetchUploadReport(key);
	}
}
