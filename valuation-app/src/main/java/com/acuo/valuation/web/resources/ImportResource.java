package com.acuo.valuation.web.resources;

import com.acuo.persist.core.ImportService;
import com.codahale.metrics.annotation.Timed;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/import")
public class ImportResource {

    private final ImportService service;
    private String[] fileNames = {"clients", "legalentities", "accounts"};

    @Inject
    public ImportResource(ImportService service) {
        this.service = service;
    }

    @GET
    @Path("reload")
    @Timed
    public Response reload() {
        service.reload(fileNames);
        return Response.status(Status.OK).build();
    }
}