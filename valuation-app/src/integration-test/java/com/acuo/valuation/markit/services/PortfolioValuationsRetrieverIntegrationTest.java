package com.acuo.valuation.markit.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.valuation.markit.responses.ResponseParser;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.responses.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class PortfolioValuationsRetrieverIntegrationTest {

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***", 1l);

	@Inject
	ResponseParser parser;

	@Test
	public void testRetrieve() throws Exception {
		PortfolioValuationsRetriever retriever = new PortfolioValuationsRetriever(markitEndPointConfig, parser);
		Response response = retriever.retrieve("2016-06-10");
		assertNotNull(response);
	}

}
