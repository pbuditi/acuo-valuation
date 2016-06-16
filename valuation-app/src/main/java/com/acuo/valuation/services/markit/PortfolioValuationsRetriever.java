package com.acuo.valuation.services.markit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.valuation.responses.Response;
import com.acuo.valuation.responses.markit.ResponseParser;

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

	/**
	 * Retrieve the results for a given valuation date
	 * 
	 * @param asOfDate,
	 *            date in DDMONYY or YYYY-MM-DD format
	 * @return response, parser from the markit report
	 */
	public Response retrieve(String asOfDate) {
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
}