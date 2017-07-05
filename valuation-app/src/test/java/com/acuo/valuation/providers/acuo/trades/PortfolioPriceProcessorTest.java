package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.jackson.PortfolioIds;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
public class PortfolioPriceProcessorTest {

    @Inject
    PortfolioPriceProcessor portfolioPriceProcessor;

    @Inject
    TradeUploadServiceTransformer tradeUploadServiceTransformer;

    @Inject
    TradeService<IRS> tradeService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    private MockWebServer server = new MockWebServer();

    List<PortfolioId> portfolioIds;

    @Inject
    private ImportService importService;

    @Before
    public void setup() throws IOException {
        server.start(8282);

        importService.reload();
        List<String> tradeIds = tradeUploadServiceTransformer.fromExcel(oneIRS.createInputStream());
        portfolioIds = tradeIds.stream().map(s -> tradeService.find(TradeId.fromString(s), 2).getPortfolio().getPortfolioId()).collect(toList());
    }

    @Test
    public void process() throws Exception {
        setMockMarkitResponse();
        portfolioPriceProcessor.process(portfolioIds);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private void setMockMarkitResponse() throws IOException {
        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(largeReport.getContent()));
        server.enqueue(new MockResponse().setBody(largeResponse.getContent()));
    }
}
