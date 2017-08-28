package com.acuo.valuation.web.resources;

import com.acuo.valuation.providers.datascope.service.intraday.DataScopeIntradayService;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/fxrates")
@Produces(MediaType.APPLICATION_JSON)
public class FXRatesResource {

    private final DataScopeIntradayService intradayService;

    @Inject
    public FXRatesResource(DataScopeIntradayService intradayService) {
        this.intradayService = intradayService;
    }

    @GET
    @Path("/update")
    @Timed
    public Response update() {
        String response = intradayService.rates();
        return Response.ok(response).build();
    }
}
