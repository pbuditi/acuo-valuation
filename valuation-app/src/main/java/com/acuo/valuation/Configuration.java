package com.acuo.valuation;

public class Configuration {

	private final AppId appId;
	private final Environment environment;

	private Configuration(Builder builder) {
		this.appId = builder.appId;
		this.environment = builder.environment;
	}

	public static Builder builder(AppId appId) {
		return new Builder(appId);
	}

	public AppId getAppId() {
		return appId;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public static class Builder {

		private final AppId appId;
		private Environment environment = Environment.TEST;

		public Builder(AppId appId) {
			this.appId = appId;
		}

		public Builder with(Environment environment) {
			this.environment = environment;
			return this;
		}

		public Configuration build() {
			return new Configuration(this);
		}
	}

}
