package com.acuo.valuation.providers.datascope.service.intraday;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.persist.entity.FXRate;
import com.acuo.persist.modules.DataImporterModule;
import com.acuo.persist.modules.DataLoaderModule;
import com.acuo.persist.modules.ImportServiceModule;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.persist.services.FXRateService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengamma.strata.basics.currency.Currency;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

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
public class DataScopeIntradayServiceImplTest {

    @Rule
    public ResourceFile request = new ResourceFile("/datascope/rates/request.json");

    @Rule
    public ResourceFile response = new ResourceFile("/datascope/rates/response.json");

    @Mock
    private DataScopeAuthService dataScopeAuthService = null;

    @Mock
    private FXRateService fxRateService = null;

    @Inject
    private ObjectMapper objectMapper = null;

    private MockWebServer server = new MockWebServer();

    private DataScopeIntradayService intradayService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        DataScopeEndPointConfig config = new DataScopeEndPointConfig(server.url("/"),
                "",
                "",
                "",
                "",
                10000,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "10",
                null);

        ClientEndPoint<DataScopeEndPointConfig> clientEndPoint = new OkHttpClient<>(httpClient, config);

        intradayService = new DataScopeIntradayServiceImpl(fxRateService, dataScopeAuthService, clientEndPoint, objectMapper);
    }

    @Test
    public void rates() throws Exception {
        server.enqueue(new MockResponse().setBody(response.getContent()));

        when(dataScopeAuthService.getToken()).thenReturn("token");
        when(fxRateService.getOrCreate(any(Currency.class),any(Currency.class))).thenReturn(new FXRate());

        intradayService.rates();

        RecordedRequest r = server.takeRequest();
        String body = r.getBody().readUtf8();
        assertThat(body).isNotNull().isEqualTo(request.getContent());
    }

}