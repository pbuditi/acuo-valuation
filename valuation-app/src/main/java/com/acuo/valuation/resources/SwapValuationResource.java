package com.acuo.valuation.resources;

import java.io.StringWriter;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.Result;
import org.modelmapper.ModelMapper;

@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

	private final VelocityEngine velocityEngine;
	private final PricingService pricingService;

	@Inject
	public SwapValuationResource(VelocityEngine velocityEngine, PricingService pricingService) {
		this.velocityEngine = velocityEngine;
		this.pricingService = pricingService;
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
	public Result price(SwapDTO swapDTO) throws Exception {

		ModelMapper modelMapper = new ModelMapper();

		IrSwap swap = modelMapper.map(swapDTO, IrSwap.class);

		Result result = pricingService.price(swap);

		return result;
	}
}