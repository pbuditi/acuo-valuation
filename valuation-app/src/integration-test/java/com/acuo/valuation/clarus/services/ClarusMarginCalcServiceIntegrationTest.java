package com.acuo.valuation.clarus.services;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.clarus.protocol.Clarus;
import com.acuo.valuation.modules.ConfigurationModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.services.MarginCalcService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.acuo.valuation.clarus.protocol.Clarus.*;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({JaxbModule.class, EncryptionModule.class, ConfigurationModule.class, EndPointModule.class, ServicesModule.class})
public class ClarusMarginCalcServiceIntegrationTest {

    @Rule
    public ResourceFile cme = new ResourceFile("/clarus/request/clarus-cme.json");

    @Rule
    public ResourceFile lch = new ResourceFile("/clarus/request/clarus-lch.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/clarus-cme.csv");

    @Rule
    public ResourceFile cmeCsv43 = new ResourceFile("/clarus/request/cme-43.csv");

    @Rule
    public ResourceFile lchCsv = new ResourceFile("/clarus/request/clarus-lch.csv");

    @Inject
    MarginCalcService service;

    @Before
    public void setup() {
    }

    @Test
    public void testResourceFileExist() throws Exception {
        assertTrue(cme.getContent().length() > 0);
        assertTrue(cme.getFile().exists());
    }

    @Test
    public void marginCalcCme() throws IOException {
        String response = service.send(cme.getContent());
        System.out.println(response);
    }

    @Test
    public void marginCalcLch() throws IOException {
        String response = service.send(lch.getContent());
        System.out.println(response);
    }

    @Test
    public void marginCalcCmeCsv() throws IOException {
        List<? extends Result> response = service.send(cmeCsv.getContent(), DataFormat.CME, DataType.SwapRegister);
        System.out.println(response);
    }

    @Test
    public void marginCalcCmeCsv43() throws IOException {
        List<? extends Result> response = service.send(cmeCsv43.getContent(), DataFormat.CME, DataType.SwapRegister);
        System.out.println(response);
    }

}
