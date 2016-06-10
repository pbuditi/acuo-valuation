package com.acuo.valuation.services;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MarkitPortfolioValuationsSender {

	private static final Logger logger = LoggerFactory.getLogger(MarkitPortfolioValuationsSender.class);

	private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";
	private static final int ONE_MINUTE = 1000 * 60; // in milliseconds
	private String url;
	private String user;
	private String password;

	public MarkitPortfolioValuationsSender(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String uploadFile(File uploadFile) throws Exception {
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

		RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), uploadFile);
		RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("username", user)
				.addFormDataPart("password", password)
				.addPart(MultipartBody.Part.createFormData("theFile", "theFile", file)).build();
		Request request = new Request.Builder().url(url).post(body).build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful())
			throw new IOException("Unexpected code " + response);

		return response.body().string();
	}

	public void fetchUploadReport(String reportKey) throws Exception {
		String report = null;

		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

		RequestBody body = new FormBody.Builder().add("username", user).add("password", password).add("key", reportKey)
				.add("version", "2").build();

		Request request = new Request.Builder().url(url).post(body).build();

		while (report == null) {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);
			String result = response.body().string();
			if (result.startsWith(STILL_PROCESSING_KEY)) {
				Thread.sleep(ONE_MINUTE); // wait before polling again
			} else {
				report = result;
			}
		}
		System.out.println(report);
	}

	class LoggingInterceptor implements Interceptor {
		@Override
		public Response intercept(Interceptor.Chain chain) throws IOException {
			Request request = chain.request();

			long t1 = System.nanoTime();
			logger.info(String.format("Sending request %s on %s%n%s", request.url(), chain.connection(),
					request.headers()));

			Response response = chain.proceed(request);

			long t2 = System.nanoTime();
			logger.info(String.format("Received response for %s in %.1fms%n%s", response.request().url(),
					(t2 - t1) / 1e6d, response.headers()));

			return response;
		}
	}
}