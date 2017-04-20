package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.persist.modules.RepositoryModule;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

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
public class DatascopeDownloadServiceTest {

    @Inject
    private DatascopeAuthService datascopeAuthService;

    @Inject
    private DatascopeScheduleService datascopeScheduleService;

    @Inject
    private DatascopeExtractionService datascopeExtractionService;

    @Inject
    private DatascopeDownloadService datascopeDownloadService;

    @Test
    public void testDownloadFile()
    {
//        String token = datascopeAuthService.getToken();
//        String scheduleId = datascopeScheduleService.scheduleFXRateExtraction(token);
//        List<String> ids = datascopeExtractionService.getExtractionFileId(token, scheduleId);
//        ids.stream().forEach(fileId -> datascopeDownloadService.downloadFile(token, fileId));
    }
}
