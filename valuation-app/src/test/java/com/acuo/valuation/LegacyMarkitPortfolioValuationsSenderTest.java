package com.acuo.valuation;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.acuo.valuation.services.LegacyMarkitPortfolioValuationsSender;

public class LegacyMarkitPortfolioValuationsSenderTest {

	@Rule
	public ResourceFile res = new ResourceFile("/test-markit-upload.xml");

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		assertTrue(res.getFile().exists());
	}

	@Test
	public void testUploadFile() throws Exception {
		LegacyMarkitPortfolioValuationsSender mps = new LegacyMarkitPortfolioValuationsSender(
				"https://pv.markit.com/upload", "acuosamedayupload", "***REMOVED***");
		String key = mps.uploadFile(res.getFile());
		System.out.println("key:" + key);
		// mps.fetchUploadReport("77503263");
	}

}
