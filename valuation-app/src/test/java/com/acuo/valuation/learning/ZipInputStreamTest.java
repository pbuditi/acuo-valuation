package com.acuo.valuation.learning;

import com.acuo.common.util.ResourceFile;
import org.apache.poi.openxml4j.util.ZipInputStreamZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ZipInputStreamTest {

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio18-05-17v2-NPV.xlsx");

    @Test
    public void testCreateZipStreamFromString() throws IOException {
        String content = excel.getContent("ISO-8859-1");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes("ISO-8859-1"));
        ZipInputStream zis = new ZipInputStream(byteArrayInputStream);
        ZipSecureFile.ThresholdInputStream tis = ZipSecureFile.addThreshold(zis);
        new ZipInputStreamZipEntrySource(tis);
    }

    @Test
    public void testCreateZipStreamFromStream() throws IOException {
        ZipInputStream zis = new ZipInputStream(excel.createInputStream());
        ZipSecureFile.ThresholdInputStream tis = ZipSecureFile.addThreshold(zis);
        new ZipInputStreamZipEntrySource(tis);
    }
}
