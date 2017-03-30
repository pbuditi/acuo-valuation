package com.acuo.valuation.providers.acuo.trades;

import com.acuo.valuation.services.TradeCacheService;
import groovy.lang.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
@Slf4j
public class LocalTradeCacheService implements TradeCacheService {

    private static Map<String, List<String>> cache = new HashMap<>();

    @Override
    public synchronized String put(List<String> trades) {
        String key = UUID.randomUUID().toString();
        cache.put(key, trades);
        return key;
    }

    @Override
    public List<String> get(String tnxId) {
        return cache.get(tnxId);
    }
}
