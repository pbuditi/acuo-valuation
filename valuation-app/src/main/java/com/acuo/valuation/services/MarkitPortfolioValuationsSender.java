package com.acuo.valuation.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class MarkitPortfolioValuationsSender {

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
		HttpPost post = new HttpPost(url);

		FileBody fileBody = new FileBody(uploadFile, ContentType.DEFAULT_BINARY, uploadFile.getName());
		StringBody userBody = new StringBody(user, ContentType.MULTIPART_FORM_DATA);
		StringBody passwordBody = new StringBody(password, ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart(uploadFile.getName(), fileBody);
		builder.addPart("username", userBody);
		builder.addPart("password", passwordBody);
		HttpEntity entity = builder.build();

		post.setEntity(entity);
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(post);
			return EntityUtils.toString(response.getEntity());
		} finally {
			post.releaseConnection();
		}
	}

	public void fetchUploadReport(String reportKey) throws Exception {
		String report = null;
		StringBody userBody = new StringBody(user, ContentType.MULTIPART_FORM_DATA);
		StringBody passwordBody = new StringBody(password, ContentType.MULTIPART_FORM_DATA);
		StringBody reportKeyBody = new StringBody(reportKey, ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("username", userBody);
		builder.addPart("password", passwordBody);
		builder.addPart("key", reportKeyBody);
		HttpEntity entity = builder.build();

		while (report == null) {

			HttpPost method = new HttpPost(url);
			method.setEntity(entity);

			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(method);
			String result = EntityUtils.toString(response.getEntity());
			if (result.startsWith(STILL_PROCESSING_KEY)) {
				Thread.sleep(ONE_MINUTE); // wait before polling again
			} else {
				report = result;
			}
		}
		// Now have the report as a string - handle it (here we just print to
		// stdout)
		System.out.println(report);
	}

	public static void main(String args[]) throws Exception {
		// Arguments are filenames of trade files to upload
		MarkitPortfolioValuationsSender mps = new MarkitPortfolioValuationsSender("https://pv.markit.com/upload",
				"acuosamedayupload", "***REMOVED***");
		List<String> uploadKeys = new ArrayList<String>(args.length);
		for (String tradeFile : args) {
			uploadKeys.add(mps.uploadFile(new File(tradeFile)));
		}
		for (String key : uploadKeys) {
			mps.fetchUploadReport(key);
		}
	}
}