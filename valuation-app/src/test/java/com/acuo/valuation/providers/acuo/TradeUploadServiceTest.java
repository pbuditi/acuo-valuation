package com.acuo.valuation.providers.acuo;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.FRA;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradingAccountService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.providers.markit.services.*;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.util.ReportHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataLoaderModule.class,
        DataImporterModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
@Slf4j
public class TradeUploadServiceTest {

    TradeUploadServiceImpl service;

    @Inject
    TradingAccountService accountService;

    @Inject
    PortfolioService portfolioService;

    @Inject
    ImportService importService;

    @Inject
    TradeService<Trade> irsService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new TradeUploadServiceImpl(accountService, portfolioService, irsService);
        importService.reload();
    }

    @Test
    public void testUploadOneIRS() {
        List<String> tradeIds = service.uploadTradesFromExcel(oneIRS.createInputStream());
        assertThat(tradeIds).isNotEmpty();
    }

    @Test
    public void testUploadAll() throws IOException {
        List<String> tradeIds = service.uploadTradesFromExcel(excel.createInputStream());
        assertThat(tradeIds).isNotEmpty().doesNotContainNull();
    }

    @Test
    public void testHandleIRSOneRowUpdate() throws IOException {
        service.uploadTradesFromExcel(oneIRS.createInputStream());
        service.uploadTradesFromExcel(oneIRS.createInputStream());
        Iterable<Trade> irses = irsService.findAll();
        assertThat(irses).isNotEmpty().hasSize(2);
    }
}