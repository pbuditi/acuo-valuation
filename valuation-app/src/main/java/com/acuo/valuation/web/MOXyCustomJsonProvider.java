package com.acuo.valuation.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;

import com.acuo.common.marshal.MarshallingEventHandler;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MOXyCustomJsonProvider extends MOXyJsonProvider {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;// getDomainClasses(genericType).contains(RequestInput.class);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isReadable(type, genericType, annotations, mediaType);
	}

	@Override
	protected void preReadFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, Unmarshaller unmarshaller) throws JAXBException {
		unmarshaller.setEventHandler(new MarshallingEventHandler());
		unmarshaller.setProperty(UnmarshallerProperties.JSON_VALUE_WRAPPER, "$");
		unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);

	}

	@Override
	protected void preWriteTo(Object object, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, Marshaller marshaller)
					throws JAXBException {
		marshaller.setEventHandler(new MarshallingEventHandler());
		marshaller.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, "$");
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
	}

}