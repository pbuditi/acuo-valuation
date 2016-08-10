package com.acuo.valuation.protocol.requests;

import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.providers.markit.protocol.requests.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RequestData {

    Map<Key<?>, Object> values = new HashMap<>();

    default <T> void put(Key<T> key, T value) {
        values.put(key, value);
    }

    default <T> T get(Key<T> key) {
        return key.type().cast(values.get(key));
    }

    @SuppressWarnings("unchecked")
    default <T> List<T> values(Class<T> value) {
        ArgChecker.notNull(value, "value");
        return values.keySet().stream().filter(k -> k.type() == value).map(k -> (T) get(k))
                .collect(Collectors.toList());
    }
}
