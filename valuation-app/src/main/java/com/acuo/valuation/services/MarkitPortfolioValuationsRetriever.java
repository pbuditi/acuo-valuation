package com.acuo.valuation.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class MarkitPortfolioValuationsRetriever {

	public MarkitPortfolioValuationsRetriever(String url, String user, String password, String theDate)
			throws Exception {

		StringBody userBody = new StringBody(user, ContentType.MULTIPART_FORM_DATA);
		StringBody passwordBody = new StringBody(password, ContentType.MULTIPART_FORM_DATA);
		StringBody theDateBody = new StringBody(theDate, ContentType.MULTIPART_FORM_DATA);
		StringBody formatBody = new StringBody("xml", ContentType.MULTIPART_FORM_DATA);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("username", userBody);
		builder.addPart("password", passwordBody);
		builder.addPart("asof", theDateBody);
		builder.addPart("format", formatBody);
		HttpEntity entity = builder.build();

		HttpPost method = new HttpPost(url);
		method.setEntity(entity);
		HttpClient client = HttpClientBuilder.create().build();
		try {
			HttpResponse response = client.execute(method);
			System.out.println(EntityUtils.toString(response.getEntity()));
		} finally {
			method.releaseConnection();
		}
	}

	public static void main(String args[]) throws Exception {
		// only argument should be the required asof date in ddmmmyyyy format
		MarkitPortfolioValuationsRetriever mps = new MarkitPortfolioValuationsRetriever(
				"https://pv.markit.com/download", "acuosamedayupload", "***REMOVED***", args[0]);
	}
}