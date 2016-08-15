package com.acuo.valuation.providers.markit.protocol.reports;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.reports.Report;
import com.google.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlunit.matchers.EvaluateXPathMatcher;
import org.xmlunit.matchers.HasXPathMatcher;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({MappingModule.class})
public class ReportParsingTest {

    @Rule
    public ResourceFile sample = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Inject
    ReportParser parser;

    @Test
    public void testResourceFilesExist() throws Exception {
        assertTrue(sample.getContent().length() > 0);
    }

    @Test
    public void testMarshalingXmlFromFiles() {
        asList(sample).stream().forEach(r -> parseAndAssertXmlFiles(r));
    }

    private void parseAndAssertXmlFiles(ResourceFile res) {
        try {
            Report report = parser.parse(res.getContent());
            String xml = parser.parse(report);

            assertThat(xml, is(notNullValue()));
            assertThat(xml, HasXPathMatcher.hasXPath("/data/header"));
            assertThat(xml, EvaluateXPathMatcher.hasXPath("/data/header/date/text()", equalTo("2016-06-22")));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
