package com.acuo.valuation.markit.reports;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.acuo.valuation.ResourceFile;

public class ReportDefinitionTest {

	@Rule
	public ResourceFile res = new ResourceFile("/sample-markit-report.xml");

	JAXBXPathReportDao dao = new JAXBXPathReportDao();

	@Test
	public void testResourceFileExist() throws Exception {
		assertTrue(res.getContent().length() > 0);
		assertTrue(res.getFile().exists());
	}

	@Test
	public void test() throws Exception {
		dao.parse(res.getContent());
	}

}
