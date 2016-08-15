package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.services.DataMapper;
import com.acuo.common.model.IrSwap;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.ConfigurationModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.services.MarginCalcService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class, EncryptionModule.class, ConfigurationModule.class, EndPointModule.class, ServicesModule.class})
public class ClarusMarginCalcServiceIntegrationTest {

    @Rule
    public ResourceFile cme = new ResourceFile("/clarus/request/clarus-cme.json");

    @Rule
    public ResourceFile lch = new ResourceFile("/clarus/request/clarus-lch.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/cme-1.csv");

    @Rule
    public ResourceFile cmeCsv43 = new ResourceFile("/clarus/request/cme-43.csv");

    @Rule
    public ResourceFile lchCsv = new ResourceFile("/clarus/request/clarus-lch.csv");

    @Inject
    MarginCalcService service;

    @Inject
    DataMapper dataMapper;

    @Before
    public void setup() {
    }

    @Test
    @Ignore
    public void testResourceFileExist() throws Exception {
        assertTrue(cme.getContent().length() > 0);
        assertTrue(cme.getFile().exists());
    }

    @Test
    @Ignore
    public void testWithMapper() throws IOException {
        List<IrSwap> trades = dataMapper.fromCmeFile(cmeCsv.getContent());
        List<? extends Result> response = service.send(trades, DataFormat.CME, DataType.SwapRegister);
        System.out.println(response);
    }
}