package com.acuo.valuation.markit.reports;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBXPathReportDao {

	private static final Logger log = LoggerFactory.getLogger(JAXBXPathReportDao.class);

	public Report parse(String xmlData) throws Exception {
		// ArgChecker.notNull(xmlData, "xmlData");
		try {
			JAXBContext jc = JAXBContextFactory.createContext(
					new Class[] { ReportDefinition.class, DateAdapter.class, ObjectFactory.class }, null);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setEventHandler(new DeserializationEventHandler());
			StringReader reader = new StringReader(xmlData);
			ReportDefinition reportDefinition = (ReportDefinition) unmarshaller.unmarshal(reader);
			Report report = reportDefinition.getReport();
			log.debug(report.toString());
			return report;
		} catch (JAXBException e) {
			String msg = e.getMessage();
			if (msg == null) {
				Throwable linkedException = e.getLinkedException();
				if (linkedException != null)
					msg = linkedException.getMessage();
			}
			String format = "can't parse plan definition xml data, exception: {}";
			log.error(format, msg);
			throw new Exception(String.format(format, msg), e);
		}
	}
}