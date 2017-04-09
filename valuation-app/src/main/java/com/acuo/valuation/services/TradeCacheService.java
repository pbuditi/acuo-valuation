package com.acuo.valuation.services;

import java.util.List;

public interface TradeCacheService {

    String put(List<String> trades);

    List<String> remove(String tnxId);

    boolean contains(String tnxId);
}
