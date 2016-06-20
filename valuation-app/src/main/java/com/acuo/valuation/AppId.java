package com.acuo.valuation;

import org.joda.convert.FromString;

import com.acuo.common.type.TypedString;
import com.acuo.common.util.ArgChecker;

public class AppId extends TypedString<AppId> {

	private static final long serialVersionUID = -7440919971063378153L;

	protected AppId(String name) {
		super(name);
	}

	@FromString
	public static AppId of(String name) {
		ArgChecker.notNull(name, "name");
		return new AppId(name);
	}
}
