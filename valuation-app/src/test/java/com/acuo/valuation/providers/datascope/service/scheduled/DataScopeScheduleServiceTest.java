package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@Slf4j
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        MappingModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        RepositoryModule.class,
        EndPointModule.class,
        ServicesModule.class})
public class DataScopeScheduleServiceTest {

    @Inject
    private DataScopeAuthService dataScopeAuthService;

    @Inject
    private DataScopeScheduleService dataScopeScheduleService;

    @Test
    public void testSheduleExtraction()
    {
//        String token = dataScopeAuthService.getToken();
//        dataScopeScheduleService.setToken(token);
//        String scheduleId = dataScopeScheduleService.scheduleFXRateExtraction();
//        Assert.assertNotNull(scheduleId);
    }

}
