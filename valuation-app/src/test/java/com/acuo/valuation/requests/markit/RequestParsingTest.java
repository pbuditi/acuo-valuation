package com.acuo.valuation.requests.markit;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;
import static org.xmlunit.matchers.ValidationMatcher.valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import com.acuo.common.marshal.jaxb.DateAdapter;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.requests.Request;
import com.acuo.valuation.requests.markit.swap.IrSwapInput;
import com.acuo.valuation.requests.markit.swap.IrSwapLegFixingInput;
import com.acuo.valuation.requests.markit.swap.IrSwapLegInput;
import com.acuo.valuation.requests.markit.swap.IrSwapLegPayDatesInput;
import com.google.inject.Inject;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ JaxbModule.class })
public class RequestParsingTest {

	@Rule
	public ResourceFile sample = new ResourceFile("/requests/markit-sample.xml");

	@Rule
	public ResourceFile schema = new ResourceFile("/requests/PresentValue.xsd");

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
		requestDataInput.swaps.add(irSwapInput());

		RequestInput requestInput = new RequestInput();
		requestInput.valuationCurrency = "USD";
		requestInput.valuationDate = new DateAdapter().unmarshal("2016-06-16");
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

	private IrSwapInput irSwapInput() throws Exception {
		IrSwapInput input = new IrSwapInput();
		input.tradeId = "TRADE_ABC";
		input.tradeDate = new DateAdapter().unmarshal("2016-06-16");
		input.book = "BOOK";
		input.legs = new ArrayList<>();
		input.legs.addAll(irSwapLegs());
		return input;
	}

	private List<IrSwapLegInput> irSwapLegs() throws Exception {

		IrSwapLegInput fixed = new IrSwapLegInput();
		fixed.id = 1;
		fixed.currency = "USD";
		fixed.fixing = null;
		fixed.spread = null;
		fixed.rate = 0.0533;
		fixed.type = "FIXED";
		fixed.daycount = "30360";
		fixed.notional = 1000000d;
		fixed.notionalxg = "NEITHER";
		fixed.payDates = payDates();

		IrSwapLegInput floating = new IrSwapLegInput();
		floating.id = 2;
		floating.currency = "USD";
		floating.fixing = fixing();
		floating.spread = 0d;
		floating.rate = null;
		floating.type = "FLOAT";
		floating.daycount = "A360";
		floating.notional = -1000000d;
		floating.notionalxg = "NEITHER";
		floating.payDates = payDates();

		return Arrays.asList(fixed, floating);
	}

	private IrSwapLegFixingInput fixing() {
		IrSwapLegFixingInput fixing = new IrSwapLegFixingInput();
		fixing.name = "USD-LIBOR-BBA";
		fixing.term = "3M";
		fixing.arrears = false;
		return null;
	}

	private IrSwapLegPayDatesInput payDates() throws Exception {
		IrSwapLegPayDatesInput payDatesInput = new IrSwapLegPayDatesInput();
		payDatesInput.startDate = new DateAdapter().unmarshal("2015-01-20");
		payDatesInput.frequency = "3M";
		payDatesInput.enddate = new DateAdapter().unmarshal("2020-01-20");
		payDatesInput.rollCode = "MODFOLL";
		payDatesInput.adjust = true;
		payDatesInput.eom = false;
		return payDatesInput;
	}
}
