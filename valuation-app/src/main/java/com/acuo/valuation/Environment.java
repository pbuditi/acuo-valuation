package com.acuo.valuation;

import java.util.HashMap;
import java.util.Map;

import org.joda.convert.FromString;

import com.acuo.common.type.TypedString;
import com.acuo.common.util.ArgChecker;

public class Environment extends TypedString<Environment> {

	private static final long serialVersionUID = 1L;

	private static Map<String, Environment> lookup = new HashMap<>();

	public static final Environment DEVELOPMENT = Environment.of("dev");
	public static final Environment DOCKER = Environment.of("docker");
	public static final Environment TEST = Environment.of("test");
	public static final Environment INTEGRATION = Environment.of("int");
	public static final Environment PRODUCTION = Environment.of("prod");

	protected Environment(String name) {
		super(name);
	}

	@FromString
	private static Environment of(String name) {
		ArgChecker.notNull(name, "name");
		Environment environment = getOrCreate(name);
		return environment;
	}

	private static Environment getOrCreate(String name) {
		Environment environment = lookup.get(name);
		synchronized (lookup) {
			if (environment == null) {
				environment = new Environment(name);
				lookup.put(name, environment);
			}
		}
		return environment;
	}

	@FromString
	public static Environment lookup(String name) {
		ArgChecker.notNull(name, "name");
		Environment environment = lookup.get(name);
		if (environment != null)
			return environment;
		else
			throw new IllegalArgumentException("Environment named [" + name + "] doesn't exist");
	}
}
