package com.acuo.valuation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MarkitPortfolioValuationsRetriever {

	private static final Logger logger = LoggerFactory.getLogger(MarkitPortfolioValuationsSender.class);

	public MarkitPortfolioValuationsRetriever(String url, String user, String password, String theDate)
			throws Exception {

		OkHttpClient client = new OkHttpClient.Builder().build();

		RequestBody body = new FormBody.Builder().add("username", user).add("password", password).add("asof", theDate)
				.add("format", "xml").build();

		Request request = new Request.Builder().url(url).post(body).build();

		Response response = client.newCall(request).execute();

		logger.info(response.body().string());

	}

	public static void main(String args[]) throws Exception {
		// only argument should be the required asof date in ddmmmyyyy format
		MarkitPortfolioValuationsRetriever mps = new MarkitPortfolioValuationsRetriever(
				"https://pv.markit.com/download", "acuosamedayupload", "***REMOVED***", args[0]);
	}
}