package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.services.TradeUploadService;
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
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

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
public class MarkitPricingProcessorTest {
    @Inject
    ImportService importService;

    @Inject
    TradeUploadService tradeUploadService;

    @Inject
    MarkitPricingProcessor pricingProcessor;

    @Inject
    TradeService<Trade> tradeService;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile largeReport = new ResourceFile("/markit/reports/large.xml");

    @Rule
    public ResourceFile largeResponse = new ResourceFile("/markit/responses/large.xml");

    MockWebServer server = new MockWebServer();

    List<Trade> swaps;

    @Before
    public void setUp() throws Exception {
        server.start(8282);

        importService.reload();
        List<String> tradeIds = tradeUploadService.uploadTradesFromExcel(oneIRS.createInputStream());
        swaps = tradeIds.stream()
                .map(id -> (IRS) tradeService.find(TradeId.fromString(id)))
                .collect(toList());
    }

    @Test
    public void process() throws Exception {
        setMockMarkitResponse();

        Collection<MarginCall> margins = pricingProcessor.process(swaps);
        assertThat(margins).isNotNull().hasSize(1);
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