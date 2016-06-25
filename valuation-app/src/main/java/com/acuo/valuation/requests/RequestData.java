package com.acuo.valuation.requests;

import com.acuo.common.util.ArgChecker;
import com.acuo.valuation.markit.requests.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RequestData {

	final Map<Key<?>, Object> values = new HashMap<>();

	default public <T> void put(Key<T> key, T value) {
		values.put(key, value);
	}

	default public <T> T get(Key<T> key) {
		return key.type().cast(values.get(key));
	}

	@SuppressWarnings("unchecked")
	default public <T> List<T> values(Class<T> value) {
		ArgChecker.notNull(value, "value");
		return values.keySet().stream().filter(k -> k.type() == value).map(k -> (T) get(k))
				.collect(Collectors.toList());
	}
}
