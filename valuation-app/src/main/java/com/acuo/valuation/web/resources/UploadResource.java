package com.acuo.valuation.web.resources;

import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.entities.UploadForm;
import com.acuo.valuation.web.entities.UploadResponse;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/upload")
public class UploadResource {

    private final TradeUploadService irsService;
    private final TradeCacheService cacheService;

    @Inject
    public UploadResource(TradeUploadService irsService, TradeCacheService cacheService) {
        this.irsService = irsService;
        this.cacheService = cacheService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response upload(@MultipartForm UploadForm entity) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream(entity.getFile());
        List<String> trades = irsService.uploadTradesFromExcel(fis);
        final UploadResponse response = new UploadResponse();
        final UploadResponse.Status success = new UploadResponse.Status(UploadResponse.StatusType.success, "test");
        final UploadResponse.Status failure = new UploadResponse.Status(UploadResponse.StatusType.success, "test");
        response.setStatuses(ImmutableList.of(success, failure));
        String tnxId = cacheService.put(trades);
        response.setTxnID(tnxId);
        return Response.status(CREATED).entity(response).build();
    }
}
