package com.acuo.valuation.web.resources;

import com.acuo.persist.entity.MarginCall;
import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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
    public MarginCallDetail upload(@MultipartForm UploadForm entity) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream(entity.getFile());
        List<String> tradeIdList = irsService.uploadTradesFromExcel(fis);
        PricingResults results = pricingService.priceTradeIds(tradeIdList);
        List<MarginCall> marginCalls = resultProcessor.process(results);
        return MarginCallDetail.of(marginCalls);
    }
}
