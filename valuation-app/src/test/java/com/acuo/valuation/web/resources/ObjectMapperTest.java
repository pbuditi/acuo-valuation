package com.acuo.valuation.web.resources;

import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.valuation.modules.EndPointModule;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.modules.ServicesModule;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class})
public class ObjectMapperTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    public void serialiseSwapTrade() throws IOException {
        String swapTrade = objectMapper.writeValueAsString(SwapHelper.createTrade());
        SwapTrade trade = objectMapper.readValue(swapTrade, SwapTrade.class);
        assertThat(trade).isNotNull();
        assertThat(trade.getInfo().getTradeId()).isEqualTo("tradeId");
    }


}
