package com.acuo.valuation.services.markit;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.valuation.utils.LoggingInterceptor;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PortfolioValuationsSender {

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsSender.class);

	private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";
	private static final int ONE_MINUTE_IN_MILLISECONDS = 1000 * 60;

	private MarkitEndPointConfig markitEndPointConfig;

	@Inject
	public PortfolioValuationsSender(MarkitEndPointConfig markitEndPointConfig) {
		this.markitEndPointConfig = markitEndPointConfig;
	}

	public String uploadFile(File uploadFile) throws Exception {
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

		RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), uploadFile);
		RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("username", markitEndPointConfig.username())
				.addFormDataPart("password", markitEndPointConfig.password())
				.addPart(MultipartBody.Part.createFormData("theFile", "theFile", file)).build();
		Request request = new Request.Builder().url(markitEndPointConfig.url()).post(body).build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			LOG.error("Response is unsuccesful {}", response);
			throw new IOException("Unexpected code " + response);
		}

		return response.body().string();
	}

	public void fetchUploadReport(String reportKey) throws Exception {
		String report = null;

		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

		RequestBody body = new FormBody.Builder().add("username", markitEndPointConfig.username())
				.add("password", markitEndPointConfig.password()).add("key", reportKey).add("version", "2").build();

		Request request = new Request.Builder().url(markitEndPointConfig.url()).post(body).build();

		while (report == null) {
			Response response = client.newCall(request).execute();
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);
			String result = response.body().string();
			if (result.startsWith(STILL_PROCESSING_KEY)) {
				Thread.sleep(ONE_MINUTE_IN_MILLISECONDS);
			} else {
				report = result;
			}
		}
		LOG.debug(report);
	}
}