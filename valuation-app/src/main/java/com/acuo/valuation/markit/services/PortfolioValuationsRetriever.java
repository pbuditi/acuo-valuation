package com.acuo.valuation.markit.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BinaryOperator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.valuation.markit.responses.ResponseParser;
import com.acuo.valuation.responses.Response;
import com.acuo.valuation.responses.Value;
import com.acuo.valuation.services.Result;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PortfolioValuationsRetriever {

	private static final String ERROR_MSG = "Error occurred while retrieving markit results for the date {}";

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsRetriever.class);

	private MarkitEndPointConfig markitEndPointConfig;
	private ResponseParser parser;

	@Inject
	public PortfolioValuationsRetriever(MarkitEndPointConfig markitEndPointConfig, ResponseParser parser) {
		this.markitEndPointConfig = markitEndPointConfig;
		this.parser = parser;
	}

	public Result retrieve(LocalDate valuationDate, String tradeId) {
		Response results = retrieve(valuationDate.format(DateTimeFormatter.ofPattern("YYYY-MM-DD")));
		Value result = results.values().stream().filter(v -> tradeId.equals(v.getTradeId())).reduce(thereCanBeOnlyOne())
				.get();
		return new SwapResult(result);
	}

	/**
	 * Retrieve the results for a given valuation date
	 * 
	 * @param asOfDate,
	 *            date in DDMONYY or YYYY-MM-DD format
	 * @return response, parser from the markit report
	 */
	Response retrieve(String asOfDate) {
		Request request = request(asOfDate);
		try {
			OkHttpClient client = new OkHttpClient.Builder().build();
			okhttp3.Response response = client.newCall(request).execute();
			String result = response.body().string();
			if (LOG.isDebugEnabled())
				LOG.debug(result);
			return parser.parse(result);
		} catch (Exception e) {
			LOG.error(ERROR_MSG, asOfDate, e);
			throw new RuntimeException(String.format(ERROR_MSG, asOfDate), e);
		}
	}

	private Request request(String asOfDate) {
		FormBody.Builder bodyBuilder = new FormBody.Builder();
		bodyBuilder.add("username", markitEndPointConfig.username());
		bodyBuilder.add("password", markitEndPointConfig.password());
		bodyBuilder.add("asof", asOfDate);
		bodyBuilder.add("format", "xml");
		RequestBody body = bodyBuilder.build();

		Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(markitEndPointConfig.url());
		requestBuilder.post(body);
		return requestBuilder.build();
	}

	private static <T> BinaryOperator<T> thereCanBeOnlyOne() {
		return (a, b) -> {
			throw new RuntimeException("Duplicate elements found: " + a + " and " + b);
		};
	}
}