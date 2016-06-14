package com.acuo.valuation.services.markit;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.reports.markit.ReportParser;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class PortfolioValuationsRetrieverTest {

	private MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig("https://pv.markit.com/upload",
			"acuosamedayupload", "***REMOVED***");

	@Inject
	ReportParser parser;

	@Test
	public void testRetrieve() throws Exception {
		PortfolioValuationsRetriever retriever = new PortfolioValuationsRetriever(markitEndPointConfig, parser);
		Report report = retriever.retrieve("2016-06-10");
		assertNotNull(report);
	}

}
