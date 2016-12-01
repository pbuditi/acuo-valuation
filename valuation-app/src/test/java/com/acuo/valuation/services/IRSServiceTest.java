package com.acuo.valuation.services;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.Neo4jPersistModule;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.MappingModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, MappingModule.class, Neo4jPersistModule.class})
public class IRSServiceTest {

    IRSService service;

    @Inject
    Neo4jPersistService session;

    @Before
    public void setup()
    {
        service = new IRSServiceImpl(session);
    }

    @Test
    public void testUploadIRS() throws FileNotFoundException
    {
        FileInputStream fis = new FileInputStream("src/test/resources/excel/Exposures.xlsx");
        service.uploadIRS(fis);

    }
}
