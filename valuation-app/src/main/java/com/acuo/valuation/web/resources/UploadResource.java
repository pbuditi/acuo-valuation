package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.MarginCall;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
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
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
@Path("/upload")
public class UploadResource {

    private final TradeUploadService irsService;
    private final PricingService pricingService;
    private final MarkitValuationProcessor resultProcessor;

    @Inject
    public UploadResource(TradeUploadService irsService,
                          PricingService pricingService,
                          MarkitValuationProcessor resultProcessor) {
        this.irsService = irsService;
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
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
        response.setTrades(trades);
        return Response.status(CREATED).entity(response).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response generate(List<String> trades) {
        PricingResults results = pricingService.priceTradeIds(trades);
        List<MarginCall> marginCalls = resultProcessor.process(results);
        return Response.status(OK).entity(MarginCallDetail.of(marginCalls)).build();
    }
}
