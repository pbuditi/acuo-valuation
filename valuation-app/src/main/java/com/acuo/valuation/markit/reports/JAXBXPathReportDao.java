package com.acuo.valuation.markit.reports;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acuo.common.marshal.jaxb.MarshallingEventHandler;
import com.acuo.common.util.ArgChecker;

public class JAXBXPathReportDao {

	private static final Logger log = LoggerFactory.getLogger(JAXBXPathReportDao.class);

	private static final String format = "can't parse plan definition xml data, exception: {}";

	private JAXBContext jc;

	public JAXBXPathReportDao() {
		try {
			jc = JAXBContextFactory.createContext(new Class[] { ReportDefinition.class, ObjectFactory.class },
					Collections.EMPTY_MAP);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}

	public Report parse(String xmlData) throws Exception {
		ArgChecker.notNull(xmlData, "xmlData");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		unmarshaller.setEventHandler(new MarshallingEventHandler());
		StringReader reader = new StringReader(xmlData);
		try {
			ReportDefinition reportDefinition = (ReportDefinition) unmarshaller.unmarshal(reader);
			return reportDefinition.getReport();
		} catch (JAXBException e) {
			String msg = errorMessage(e);
			log.error(format, msg);
			throw new Exception(String.format(format, msg), e);
		}
	}

	public String parse(Report report) throws Exception {
		ArgChecker.notNull(report, "report");

		Marshaller marshaller = jc.createMarshaller();
		StringWriter writer = new StringWriter();

		try {
			marshaller.marshal(report, writer);
		} catch (JAXBException e) {
			String msg = errorMessage(e);
			log.error(format, msg);
			throw new Exception(String.format(format, msg), e);
		}

		return writer.toString();
	}

	private String errorMessage(JAXBException jaxbException) {
		String msg = jaxbException.getMessage();
		if (msg == null) {
			Throwable linkedException = jaxbException.getLinkedException();
			if (linkedException != null)
				msg = linkedException.getMessage();
		}
		return msg;
	}
}