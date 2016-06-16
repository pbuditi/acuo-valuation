package com.acuo.valuation.modules;

import java.util.Arrays;
import java.util.List;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.marshal.jaxb.JaxbContextFactory;
import com.acuo.common.marshal.jaxb.JaxbMarshaller;
import com.acuo.common.marshal.jaxb.JaxbTypes;
import com.acuo.common.marshal.jaxb.MoxyJaxbContextFactory;
import com.acuo.valuation.requests.markit.RequestInput;
import com.acuo.valuation.requests.markit.swap.IrSwapInput;
import com.acuo.valuation.responses.markit.ResponseDefinition;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class JaxbModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(JaxbContextFactory.class).to(MoxyJaxbContextFactory.class);
		bind(Marshaller.class).to(JaxbMarshaller.class);
	}

	@Provides
	@JaxbTypes
	List<Class<?>> types() {
		return Arrays.asList(new Class<?>[] { ResponseDefinition.class, RequestInput.class, IrSwapInput.class });
	}

}
