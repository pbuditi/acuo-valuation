package com.acuo.valuation.services.markit;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.responses.Response;
import com.acuo.valuation.responses.markit.ResponseParser;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class PortfolioValuationsRetrieverTest {

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***");

	@Inject
	ResponseParser parser;

	@Test
	public void testRetrieve() throws Exception {
		PortfolioValuationsRetriever retriever = new PortfolioValuationsRetriever(markitEndPointConfig, parser);
		Response response = retriever.retrieve("2016-06-10");
		assertNotNull(response);
	}

}
