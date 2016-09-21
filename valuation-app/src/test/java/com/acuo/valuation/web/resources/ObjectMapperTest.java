package com.acuo.valuation.web.resources;

import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({SwapValuationResourceTest.MockServiceModule.class, MappingModule.class, EndPointModule.class, ServicesModule.class, ResourcesModule.class})
public class ObjectMapperTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    public void serialiseSwapTrade() throws IOException {
        String swapTrade = objectMapper.writeValueAsString(SwapHelper.createTrade());
        System.out.println(swapTrade);
        SwapTrade trade = objectMapper.readValue(swapTrade, SwapTrade.class);
    }


}
