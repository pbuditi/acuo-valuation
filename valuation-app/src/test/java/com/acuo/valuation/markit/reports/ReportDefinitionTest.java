package com.acuo.valuation.markit.reports;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.marshal.jaxb.JaxbContextFactory;
import com.acuo.common.marshal.jaxb.JaxbMarshaller;
import com.acuo.common.marshal.jaxb.JaxbTypes;
import com.acuo.common.marshal.jaxb.MoxyJaxbContextFactory;
import com.acuo.common.util.ResourceFile;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class ReportDefinitionTest {

	@Rule
	public ResourceFile res = new ResourceFile("/sample-markit-report.xml");

	JAXBXPathReportDao dao = new JAXBXPathReportDao();

	@Inject
	ReportDao reportDao;

	@Before
	public void init() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(JaxbContextFactory.class).to(MoxyJaxbContextFactory.class);
				bind(Marshaller.class).to(JaxbMarshaller.class);
			}

			@Provides
			@JaxbTypes
			List<Class<?>> types() {
				return Arrays.asList(new Class<?>[] { ReportDefinition.class });
			}
		});
		injector.injectMembers(this);
	}

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		assertTrue(res.getFile().exists());
	}

	@Test
	public void testJAXBDao() throws Exception {
		Report report = dao.parse(res.getContent());
		assertNotNull(report);
	}

	@Test
	public void testCommonDao() throws Exception {
		Report report = reportDao.parse(res.getContent());
		String xml = reportDao.parse(report);

		assertThat(xml, isSimilarTo(res.getContent()).ignoreWhitespace()
				.withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)).throwComparisonFailure());
	}
}
