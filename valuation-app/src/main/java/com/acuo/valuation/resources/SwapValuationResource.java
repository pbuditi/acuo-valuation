package com.acuo.valuation.resources;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapLeg;
import com.acuo.valuation.markit.requests.swap.IrSwapLegPayDates;
import com.acuo.valuation.markit.requests.swap.IrSwapLegPayDatesInput;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.requests.dto.SwapLegPayDatesDTO;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.results.SwapResult;
import com.acuo.valuation.results.dto.SwapResultDTO;
import com.acuo.valuation.services.PricingService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;

@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

	private final PricingService pricingService;
	private final VelocityEngine velocityEngine;
	private final ModelMapper mapper;

	PropertyMap<SwapLegPayDatesDTO, IrSwapLegPayDates> swapMap = new PropertyMap<SwapLegPayDatesDTO, IrSwapLegPayDates>() {
		protected void configure() {
			map().setFrequency(source.getFreq());
		}
	};

	@Inject
	public SwapValuationResource(PricingService pricingService, VelocityEngine velocityEngine, ModelMapper mapper) {
		this.velocityEngine = velocityEngine;
		this.pricingService = pricingService;
		this.mapper = mapper;
	}

	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/")
	public String hello() {
		StringWriter writer = new StringWriter();
		velocityEngine.mergeTemplate("velocity/swaps.vm", "UTF-8", new VelocityContext(), writer);
		return writer.toString();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/value")
	public SwapResultDTO price(SwapDTO swapDTO) throws Exception {
		mapper.addMappings(swapMap);
		IrSwap swap = mapper.map(swapDTO, IrSwap.class);
		SwapResult result = pricingService.price(swap);
		SwapResultDTO resultDTO = mapper.map(result, SwapResultDTO.class);
		return resultDTO;
	}
}