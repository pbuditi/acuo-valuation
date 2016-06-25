package com.acuo.valuation.mapping;

import com.acuo.common.marshal.Marshaller;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.modules.JaxbModule;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.util.SwapHelper;
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
@GuiceJUnitRunner.GuiceModules({JaxbModule.class})
public class MapperTest {

    @Rule
    public ResourceFile swapDTOResource = new ResourceFile("/requests/dto-swap-test-01.json");

    @Inject
    @Named("json")
    Marshaller marshaller;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void validateMappingFromSwapDTOtoIRSwap() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(SwapDTO.class, IrSwap.class);
        modelMapper.validate();
    }

    @Test
    @Ignore
    public void validateMappingFromIRSwaptoSwapDTO() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(IrSwap.class, SwapDTO.class);
        modelMapper.validate();
    }

    @Test
    public void testMappingSwapDTOtoIRSwap() throws Exception {
        SwapDTO swapDTO = marshaller.unmarshal(swapDTOResource.getContent(), SwapDTO.class);
        ModelMapper modelMapper = new ModelMapper();
        IrSwap swap = modelMapper.map(swapDTO, IrSwap.class);

        assertThat(swap).isNotNull();
    }

    @Test
    @Ignore
    public void testMappingFromIrSwapToSwapDTO() throws Exception {
        IrSwapInput swapInput = SwapHelper.irSwapInput();
        IrSwap swap = new IrSwap(swapInput);

        ModelMapper modelMapper = new ModelMapper();

        SwapDTO swapDTO = modelMapper.map(swap, SwapDTO.class);

        assertThat(swapDTO).isNotNull();
    }
}