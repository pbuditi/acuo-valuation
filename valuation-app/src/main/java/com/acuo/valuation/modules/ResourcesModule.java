package com.acuo.valuation.modules;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;

import com.acuo.valuation.resources.SwapValuationResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Created by aurel.avramescu on 14/06/2014.
 */
public class ResourcesModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SwapValuationResource.class);
	}

	@Provides
	public VelocityEngine getEngine() {
		Properties p = new Properties();
		try {
			p.load(getClass().getResourceAsStream("/velocity.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		VelocityEngine engine = new VelocityEngine(p);
		return engine;
	}
}