package com.acuo.valuation.reports.markit;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.reports.Report;
import com.acuo.valuation.reports.markit.ReportParser;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class ReportDefinitionTest {

	@Rule
	public ResourceFile sample = new ResourceFile("/sample-markit-report.xml");

	@Rule
	public ResourceFile valuationOn20160610 = new ResourceFile("/markit-report-20160610.xml");

	@Inject
	ReportParser parser;

	@Test
	public void testResourceFilesExist() throws Exception {
		assertTrue(sample.getContent().length() > 0);
		assertTrue(sample.getFile().exists());

		assertTrue(valuationOn20160610.getContent().length() > 0);
		assertTrue(valuationOn20160610.getFile().exists());
	}

	@Test
	public void testBothWayParsing() {
		asList(sample, valuationOn20160610).stream().forEach(r -> run(r));
	}

	public void run(ResourceFile res) {
		try {
			Report report = parser.parse(res.getContent());
			String xml = parser.parse(report);

			assertThat(xml, isSimilarTo(res.getContent()).ignoreWhitespace()
					.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).throwComparisonFailure());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
