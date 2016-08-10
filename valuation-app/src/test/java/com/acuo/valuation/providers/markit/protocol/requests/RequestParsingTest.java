package com.acuo.valuation.providers.markit.protocol.requests;

import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.protocol.requests.Request;
import com.acuo.valuation.util.SwapHelper;
import com.google.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;
import static org.xmlunit.matchers.ValidationMatcher.valid;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({JaxbModule.class})
public class RequestParsingTest {

    @Rule
    public ResourceFile sample = new ResourceFile("/markit/requests/markit-sample.xml");

    @Rule
    public ResourceFile schema = new ResourceFile("/markit/requests/PresentValue.xsd");

    @Inject
    RequestParser parser;

    @Test
    public void testResourceFilesExist() throws Exception {
        assertTrue(sample.getContent().length() > 0);
    }

    @Test
    public void testMarshalingXmlFromFiles() {
        asList(sample).stream().forEach(r -> parseAndAssertXmlFiles(r));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarshalingFromPojo() throws Exception {

        RequestDataInput requestDataInput = new RequestDataInput();
        requestDataInput.swaps = new ArrayList<>();
        requestDataInput.swaps.add(SwapHelper.irSwapInput());

        RequestInput requestInput = new RequestInput();
        requestInput.valuationCurrency = "USD";
        requestInput.valuationDate = new LocalDateAdapter().unmarshal("2016-06-16");
        requestInput.data = requestDataInput;

        Request request = MarkitRequest.of(requestInput);

        String xml = parser.parse(request);
        assertThat(xml, is(notNullValue()));
        assertThat(xml, valid(Input.fromStream(schema.getInputStream())));
    }

    @SuppressWarnings("unchecked")
    private void parseAndAssertXmlFiles(ResourceFile res) {
        try {
            Request request = parser.parse(res.getContent());
            String xml = parser.parse(request);

            assertThat(xml, is(notNullValue()));
            assertThat(xml, valid(Input.fromStream(schema.getInputStream())));
            assertThat(xml, isSimilarTo(res.getContent()).ignoreWhitespace()
                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)).throwComparisonFailure());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


}
