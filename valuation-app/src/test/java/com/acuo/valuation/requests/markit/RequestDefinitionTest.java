package com.acuo.valuation.requests.markit;

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
import com.acuo.valuation.requests.Request;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class RequestDefinitionTest {

	@Rule
	public ResourceFile sample = new ResourceFile("/requests/markit-sample.xml");

	@Inject
	RequestParser parser;

	@Test
	public void testResourceFilesExist() throws Exception {
		assertTrue(sample.getContent().length() > 0);
	}

	@Test
	public void testParsingSampleFile() {
		asList(sample).stream().forEach(r -> run(r));
	}

	public void run(ResourceFile res) {
		try {
			Request request = parser.parse(res.getContent());
			String xml = parser.parse(request);

			assertThat(xml, isSimilarTo(res.getContent()).ignoreWhitespace()
					.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).throwComparisonFailure());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
