package com.acuo.valuation.providers.acuo.trades;

import com.acuo.valuation.services.TradeCacheService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Slf4j
public class LocalTradeCacheService implements TradeCacheService {

    private static Map<String, List<String>> cache = new ConcurrentHashMap<>();

    @Override
    public synchronized String put(List<String> trades) {
        String key = UUID.randomUUID().toString();
        cache.put(key, trades);
        return key;
    }

    @Override
    public List<String> remove(String tnxId) {
        return cache.remove(tnxId);
    }

    @Override
    public boolean contains(String tnxId) {
        return cache.containsKey(tnxId);
    }
}
