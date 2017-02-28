package com.acuo.valuation.web.resources;

import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.entities.UploadForm;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Path("/upload")
public class UploadResource {

    private final TradeUploadService irsService;

    @Inject
    public UploadResource(TradeUploadService irsService) {
        this.irsService = irsService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    public MarginCallDetail upload(@MultipartForm UploadForm entity) throws IOException {

        ByteArrayInputStream fis = new ByteArrayInputStream(entity.getFile());

        return irsService.uploadTradesFromExcel(fis);
    }
}
