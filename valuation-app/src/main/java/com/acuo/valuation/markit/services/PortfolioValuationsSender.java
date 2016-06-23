package com.acuo.valuation.markit.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import javax.inject.Inject;

import com.acuo.valuation.markit.requests.MarkitRequestData;
import com.acuo.valuation.markit.requests.RequestDataInput;
import com.acuo.valuation.markit.requests.RequestInput;
import com.acuo.valuation.markit.requests.RequestParser;
import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.requests.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.valuation.reports.Report;
import com.acuo.valuation.markit.reports.ReportParser;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.utils.LoggingInterceptor;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PortfolioValuationsSender implements Sender {

	private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsSender.class);

	private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";
	private static final int ONE_MINUTE_IN_MILLISECONDS = 1000 * 60;

	private final MarkitEndPointConfig markitEndPointConfig;
	private final RequestParser requestParser;
	private final ReportParser reportParser;

	@Inject
	public PortfolioValuationsSender(MarkitEndPointConfig markitEndPointConfig, RequestParser requestParser, ReportParser reportParser) {
		this.markitEndPointConfig = markitEndPointConfig;
		this.requestParser = requestParser;
		this.reportParser = reportParser;
	}

	public Report send(IrSwap swap) {
		try {
			String file = generateFile(swap);
			return send(file);
		} catch (Exception e) {
			LOG.error("error uploading file for {} to markit pv service", swap, e);
		}
		return null;
	}

	public Report send(String file) {
		try {
			String key = uploadFile(file);
			String result = fetchUploadReport(key);
			return reportParser.parse(result);
		} catch (Exception e) {
			LOG.error("error uploading file for {} to markit pv service", file, e);
		}
		return null;
	}

	private String generateFile(IrSwap swap) throws Exception {
		LocalDate valuationDate = LocalDate.now();
		String valuationCurrency = "USD";
		IrSwapInput swapInput = new IrSwapInput(swap);
		RequestDataInput dataInput = new RequestDataInput();
		dataInput.swaps.add(swapInput);
		RequestData data = MarkitRequestData.of(dataInput);
		RequestInput input = new RequestInput(valuationDate, valuationCurrency, data);
		return requestParser.parse(input.request());

	}

	private String uploadFile(String uploadFile) throws Exception {
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

	private String fetchUploadReport(String reportKey) throws Exception {
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
				Thread.sleep(markitEndPointConfig.retryDelayInMilliseconds());
			} else {
				report = result;
			}
		}
		LOG.debug(report);
		return report;
	}
}