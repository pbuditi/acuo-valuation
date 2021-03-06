package com.acuo.valuation.providers.markit.protocol.responses;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.responses.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({MappingModule.class})
public class ResponseParsingTest {

    @Rule
    public ResourceFile sample = new ResourceFile("/markit/responses/markit-sample.xml");

    @Rule
    public ResourceFile valuationOn20160610 = new ResourceFile("/markit/responses/markit-20160610.xml");

    @Inject
    ResponseParser parser;

    @Test
    public void testResourceFilesExist() throws Exception {
        assertTrue(sample.getContent().length() > 0);
        assertTrue(valuationOn20160610.getContent().length() > 0);
    }

    @Test
    public void testBothWayParsing() {
        asList(sample, valuationOn20160610).stream().forEach(r -> run(r));
    }

    public void run(ResourceFile res) {
        try {
            Response response = parser.parse(res.getContent());
            String xml = parser.parse(response);

            assertThat(xml, isSimilarTo(res.getContent()).ignoreWhitespace()
                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).throwComparisonFailure());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
