package com.acuo.valuation.mapping;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.modules.MappingModule;
import com.acuo.valuation.protocol.requests.dto.SwapDTO;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({MappingModule.class})
public class MapperTest {

    @Rule
    public ResourceFile swapDTOResource = new ResourceFile("/json/swap-request.json");

    @Inject
    @Named("json")
    Marshaller marshaller;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    @Ignore
    public void validateMappingFromSwapDTOtoIRSwap() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(SwapDTO.class, SwapTrade.class);
        modelMapper.validate();
    }

    @Test
    @Ignore
    public void validateMappingFromIRSwaptoSwapDTO() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(SwapTrade.class, SwapDTO.class);
        modelMapper.validate();
    }

    @Test
    @Ignore
    public void testMappingSwapDTOtoIRSwap() throws Exception {
        SwapDTO swapDTO = marshaller.unmarshal(swapDTOResource.getContent(), SwapDTO.class);
        ModelMapper modelMapper = new ModelMapper();
        SwapTrade swap = modelMapper.map(swapDTO, SwapTrade.class);

        assertThat(swap).isNotNull();
    }
}