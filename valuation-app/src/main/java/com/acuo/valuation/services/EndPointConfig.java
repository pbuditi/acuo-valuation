package com.acuo.valuation.services;

import java.util.concurrent.TimeUnit;

public interface EndPointConfig {

    int connectionTimeOut();
    TimeUnit connectionTimeOutUnit();

}
