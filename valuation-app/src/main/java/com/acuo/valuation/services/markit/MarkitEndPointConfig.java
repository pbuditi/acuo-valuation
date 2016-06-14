package com.acuo.valuation.services.markit;

import com.acuo.common.util.ArgChecker;

public class MarkitEndPointConfig {

	private final String url;
	private final String username;
	private final String password;

	public MarkitEndPointConfig(String url, String username, String password) {
		ArgChecker.notEmpty(url, "url");
		ArgChecker.notEmpty(username, "username");
		ArgChecker.notEmpty(password, "password");
		this.url = url;
		this.username = username;
		this.password = password;
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

}
