package com.acuo.valuation.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class LegacyMarkitPortfolioValuationsSender {
	private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";
	private static final int ONE_MINUTE = 1000 * 60; // in milliseconds
	private String url;
	private String user;
	private String password;

	public LegacyMarkitPortfolioValuationsSender(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String uploadFile(File uploadFile) throws Exception {
		PostMethod method = new PostMethod(url);
		Part[] parts = new Part[3];
		parts[0] = new StringPart("username", user);
		parts[1] = new StringPart("password", password);
		parts[2] = new FilePart("theFile", uploadFile);
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		try {
			HttpClient client = new HttpClient();
			client.executeMethod(method);
			return method.getResponseBodyAsString();
		} finally {
			method.releaseConnection();
		}
	}

	public void fetchUploadReport(String reportKey) throws Exception {
		String report = null;
		while (report == null) {
			NameValuePair formArgs[] = { new NameValuePair("username", user), new NameValuePair("password", password),
					new NameValuePair("key", reportKey) };
			PostMethod method = new PostMethod(url);
			method.setRequestBody(formArgs);
			HttpClient client = new HttpClient();
			client.executeMethod(method);
			String response = method.getResponseBodyAsString();
			if (response.startsWith(STILL_PROCESSING_KEY)) {
				Thread.sleep(ONE_MINUTE); // wait before polling again
			} else {
				report = response;
			}
		}
		// Now have the report as a string - handle it (here we just print to
		// stdout)
		System.out.println(report);
	}

	public static void main(String args[]) throws Exception {
		// Arguments are filenames of trade files to upload
		LegacyMarkitPortfolioValuationsSender mps = new LegacyMarkitPortfolioValuationsSender(
				"https://pv.markit.com/upload", "myUserName", "myPassword");
		List<String> uploadKeys = new ArrayList<String>(args.length);
		for (String tradeFile : args) {
			uploadKeys.add(mps.uploadFile(new File(tradeFile)));
		}
		for (String key : uploadKeys) {
			mps.fetchUploadReport(key);
		}
	}
}