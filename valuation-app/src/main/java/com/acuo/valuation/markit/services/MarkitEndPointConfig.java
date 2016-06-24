package com.acuo.valuation.markit.services;

import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.services.EndPointConfig;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

@ToString
public class MarkitEndPointConfig implements EndPointConfig {

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

	@Override
	public String url() {
		return url;
	}

	@Override
	public String username() {
		return username;
	}

	@Override
	public String password() {
		return password;
	}

	@Override
	public Long retryDelayInMilliseconds() { return retryDelayInMilliseconds; }

}
