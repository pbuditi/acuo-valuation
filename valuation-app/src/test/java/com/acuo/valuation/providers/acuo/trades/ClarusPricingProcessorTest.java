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
public class ClarusPricingProcessorTest {

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private ClarusVMProcessorImpl vmProcessor = null;

    @Inject
    private ClarusIMProcessorImpl imProcessor = null;

    @Inject
    private TradeService<Trade> tradeService = null;

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile response = new ResourceFile("/clarus/response/clarus-lch.json");

    private MockWebServer server = new MockWebServer();

    private List<Trade> swaps;

    @Before
    public void setup() throws IOException {
        server.start(8282);

        importService.reload();
        List<String> tradeIds = tradeUploadService.fromExcel(oneIRS.createInputStream());
        swaps = tradeIds.stream()
                .map(id -> (IRS) tradeService.find(TradeId.fromString(id)))
                .collect(toList());
    }

    @Test
    public void testProcessor() throws IOException {
        server.enqueue(new MockResponse().setBody(response.getContent()));
        server.enqueue(new MockResponse().setBody(response.getContent()));

        Collection<MarginCall> margins = vmProcessor.process(swaps);
        assertThat(margins).isNotNull().hasSize(1);

        margins = imProcessor.process(swaps);
        assertThat(margins).isNotNull().hasSize(1);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

}