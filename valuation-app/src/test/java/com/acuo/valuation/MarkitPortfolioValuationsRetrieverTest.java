package com.acuo.valuation;

import org.junit.Test;

import com.acuo.valuation.services.MarkitPortfolioValuationsRetriever;

public class MarkitPortfolioValuationsRetrieverTest {

	@Test
	public void testUploadFile() throws Exception {
		// only argument should be the required asof date in DDMONYY or
		// YYYY-MM-DD format
		MarkitPortfolioValuationsRetriever mps = new MarkitPortfolioValuationsRetriever("https://pv.markit.com/upload",
				"acuosamedayupload", "***REMOVED***", "2016-06-10");
	}

}
