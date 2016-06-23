package com.acuo.valuation.markit.services;

import com.acuo.common.util.ArgChecker;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

@ToString
public class MarkitEndPointConfig {

	private final String url;
	private final String username;
	private final String password;
	private final Long retryDelayInMilliseconds;

	public MarkitEndPointConfig(String url, String username, String password, Long retryDelayInMinute) {
		ArgChecker.notEmpty(url, "url");
		ArgChecker.notEmpty(username, "username");
		ArgChecker.notEmpty(password, "password");
		this.url = url;
		this.username = username;
		this.password = password;
		this.retryDelayInMilliseconds = TimeUnit.MINUTES.toMillis(retryDelayInMinute);
	}

	public String url() {
		return url;
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	public Long retryDelayInMilliseconds() { return retryDelayInMilliseconds; }

}
