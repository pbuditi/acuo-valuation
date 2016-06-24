package com.acuo.valuation.services;

public interface EndPointConfig {
    String url();

    String username();

    String password();

    Long retryDelayInMilliseconds();
}
