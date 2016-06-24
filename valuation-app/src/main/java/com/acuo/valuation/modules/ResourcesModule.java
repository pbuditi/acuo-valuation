package com.acuo.valuation.modules;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;

import com.acuo.valuation.resources.SwapValuationResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.modelmapper.ModelMapper;

public class ResourcesModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SwapValuationResource.class);
		bind(ModelMapper.class);
	}

	@Provides
	public VelocityEngine getEngine() {
		try {
			Properties p = new Properties();
			p.load(getClass().getResourceAsStream("/velocity.properties"));
			VelocityEngine engine = new VelocityEngine(p);
			return engine;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}