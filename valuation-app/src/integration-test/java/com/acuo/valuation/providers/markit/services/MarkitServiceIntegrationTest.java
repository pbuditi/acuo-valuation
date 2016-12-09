package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.opengamma.strata.collect.result.Result;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class,
                                MappingModule.class,
                                EncryptionModule.class,
                                Neo4jPersistModule.class,
                                ParsersModule.class,
                                EndPointModule.class,
                                ServicesModule.class})
public class MarkitServiceIntegrationTest {

    @Inject
    PricingService pricingService;

    @Test
    @Ignore
    public void testPriceASwap() {
        SwapTrade swap = SwapHelper.createTrade();
        PricingResults pricingResults = pricingService.price(ImmutableList.of(swap));

        assertThat(pricingResults).isNotNull();
        ImmutableList<Result<MarkitValuation>> results = pricingResults.getResults();
        assertThat(results).isNotEmpty();
        for (Result result: results) {
            assertThat(result.isSuccess()).isTrue();
        }
    }
}
