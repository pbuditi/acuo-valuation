package com.acuo.valuation.learning;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.modules.*;
import com.acuo.persist.services.MarginCallService;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.modules.ConfigurationTestModule;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
public class MarginCallDetailTest {


    @Inject
    ImportService importService;

    @Inject
    MarginCallService marginCallService;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        importService.reload();
    }

    @Test
    public void testBuilder()
    {
        Iterable<MarginCall> marginCalls = marginCallService.findAll();
        List<MarginCall> marginCallList = new ArrayList<MarginCall>();
        marginCalls.forEach(marginCall -> marginCallList.add(marginCallService.findById(marginCall.getMarginCallId(), 2)));
        MarginCallDetail marginCallDetail = MarginCallDetail.of(marginCallList);
        log.info(marginCallDetail.toString());
    }
}
