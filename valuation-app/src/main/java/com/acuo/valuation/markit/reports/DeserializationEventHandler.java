package com.acuo.valuation.markit.reports;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeserializationEventHandler implements ValidationEventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DeserializationEventHandler.class);

	@Override
	public boolean handleEvent(ValidationEvent event) {
		LOG.warn("Error during XML conversion: {}", event);

		if (event.getLinkedException() instanceof NumberFormatException)
			return false;

		return true;
	}

}