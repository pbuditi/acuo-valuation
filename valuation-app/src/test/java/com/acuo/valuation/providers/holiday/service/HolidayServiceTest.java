package com.acuo.valuation.providers.holiday.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.common.http.client.OkHttpClient;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.holiday.services.HolidayEndPointConfig;
import com.acuo.valuation.providers.holiday.services.HolidayService;
import com.acuo.valuation.providers.holiday.services.HolidayServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class HolidayServiceTest {


    HolidayService holidayService;

    MockWebServer server = new MockWebServer();

    @Before
    public void setup() throws IOException {
        server.start();

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

        HolidayEndPointConfig config = new HolidayEndPointConfig("http", server.getHostName(), server.getPort(), "test" ,"key", 10000);

        ClientEndPoint<HolidayEndPointConfig> clientEndPoint = new OkHttpClient<>(httpClient, config);

        holidayService = new HolidayServiceImpl(clientEndPoint);

    }

    @Test
    public void testQueryHoliday()
    {
        server.enqueue(new MockResponse().setBody("True"));
        boolean result = holidayService.queryHoliday(LocalDate.of(2017,07,12));
        Assert.assertTrue(result);
    }
}
