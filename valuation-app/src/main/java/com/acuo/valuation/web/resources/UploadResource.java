package com.acuo.valuation.web.resources;

import com.acuo.valuation.services.IRSService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/upload")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
public class UploadResource {

    private final IRSService irsService;

    @Inject
    public UploadResource(IRSService irsService) {
        this.irsService = irsService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response upload(InputStream input) {
        irsService.uploadIRS(input);
        return Response.ok().build();
    }
}
