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
import org.junit.*;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class, EncryptionModule.class, ConfigurationModule.class, EndPointModule.class, ServicesModule.class})
public class ClarusMarginCalcServiceIntegrationTest {

    @Rule
    public ResourceFile cmeJsonResponse = new ResourceFile("/clarus/response/clarus-cme.json");

    @Rule
    public ResourceFile cmeCsv = new ResourceFile("/clarus/request/cme-1.csv");

    @Inject
    MarginCalcService service;

    @Inject
    DataMapper dataMapper;

    @Before
    public void setup() {
    }

    @Test
    public void testResourceFileExist() throws Exception {
        assertTrue(cmeJsonResponse.getContent().length() > 0);
        assertTrue(cmeJsonResponse.getFile().exists());
    }

    @Test
    public void testWithMapper() throws IOException {
        List<IrSwap> trades = dataMapper.fromCmeFile(cmeCsv.getContent());
        List<? extends Result> response = service.send(trades, DataFormat.CME, DataType.SwapRegister);
        Assert.assertThat(response, isJson());
        Assert.assertThat(response, jsonEquals(cmeJsonResponse.getContent()).when(IGNORING_EXTRA_FIELDS));
    }
}