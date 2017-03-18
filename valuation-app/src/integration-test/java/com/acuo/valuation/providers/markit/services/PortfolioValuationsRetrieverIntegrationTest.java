package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.markit.protocol.responses.ResponseParser;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.protocol.responses.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

@Ignore
@RunWith(GuiceJUnitRunner.class)
@GuiceModules({EndPointModule.class, ServicesModule.class, MappingModule.class})
public class PortfolioValuationsRetrieverIntegrationTest {

    @Inject
    ClientEndPoint clientEndPoint;

    @Inject
    ResponseParser parser;

    @Before
    public void setUp() {

    }

    @Test
    public void testRetrieve() throws Exception {
        PortfolioValuationsRetriever retriever = new PortfolioValuationsRetriever(clientEndPoint, parser);
        PricingResults results = retriever.retrieve(LocalDate.of(2016,6,10), new ArrayList<String>());
        assertNotNull(results);
    }

}