package com.acuo.valuation.services;

import java.util.List;

public interface TradeCacheService {

    String put(List<String> trades);

    List<String> get(String tnxId);
}
