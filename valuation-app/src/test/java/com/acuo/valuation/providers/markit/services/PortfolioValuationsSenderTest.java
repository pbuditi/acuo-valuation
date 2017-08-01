package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.builders.TradeConverter;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.services.TradeUploadService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

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
public class PortfolioValuationsSenderTest {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    @Rule
    public ResourceFile oneIRS = new ResourceFile("/excel/OneIRS.xlsx");

    @Rule
    public ResourceFile report = new ResourceFile("/markit/reports/markit-test-01.xml");

    @Inject
    private ReportParser reportParser = null;

    @Inject
    @Named("markit")
    private Transformer<com.acuo.common.model.trade.Trade> markitTransformer = null;

    @Inject
    private ImportService importService = null;

    @Inject
    private TradeUploadService tradeUploadService = null;

    @Inject
    private TradeService<Trade> tradeService = null;

    private MockWebServer server = new MockWebServer();

    private PortfolioValuationsSender sender;

    private List<com.acuo.common.model.trade.Trade> trades;

    @Before
    public void setUp() throws Exception {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient
                                                     .Builder()
                                                     .addInterceptor(new LoggingInterceptor())
                                                     .build();
        MarkitEndPointConfig markitEndPointConfig = new MarkitEndPointConfig(server.url("/"), "", "",
                "username", "password", "0", "10000", "false");

        OkHttpClient<MarkitEndPointConfig> client = new MarkitClient(httpClient, markitEndPointConfig);

        sender = new PortfolioValuationsSender(client, reportParser, markitTransformer);

        importService.reload();

        final List<String> tradeIds = tradeUploadService.fromExcel(oneIRS.createInputStream());

        trades = tradeIds.stream()
                .map(id -> tradeService.find(TradeId.fromString(id)))
                .map(TradeConverter::buildTrade)
                .collect(toList());
    }

    @Test
    public void send() throws Exception {

        server.enqueue(new MockResponse().setBody("key"));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(STILL_PROCESSING_KEY));
        server.enqueue(new MockResponse().setBody(report.getContent()));

        Report r = sender.send(trades, LocalDate.now());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("username")
                .contains("password")
                /*.contains(swap.getTradeId())*/;

        IntStream.range(1, 3).forEach(i -> {
            try {
                RecordedRequest key = server.takeRequest();
                String keyBody = key.getBody().readUtf8();
                System.out.println(keyBody);
                assertThat(keyBody).contains("username=username")
                        .contains("password=password")
                        .contains("key=key")
                        .contains("version=2");
            } catch (InterruptedException e) {
                // do nothing
            }
        });
        assertThat(r).isNotNull();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }


}