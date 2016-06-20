package com.acuo.valuation.resources;

import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.acuo.valuation.requests.markit.RequestInput;
import com.google.inject.Inject;

@Path("/swaps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SwapValuationResource {

	@Inject
	private VelocityEngine velocityEngine;

	@GET
	@Produces({ MediaType.TEXT_HTML })
	@Path("/")
	public String hello() {
		StringWriter writer = new StringWriter();
		velocityEngine.mergeTemplate("velocity/simple.vm", "UTF-8", new VelocityContext(), writer);
		return writer.toString();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/value")
	public RequestInput price(RequestInput input) throws Exception {

		System.out.println("Trade ID = " + input);

		return input;
	}
}