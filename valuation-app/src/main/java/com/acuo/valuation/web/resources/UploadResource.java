package com.acuo.valuation.web.resources;

import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.Portfolio;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.services.PortfolioManager;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.entities.UploadForm;
import com.acuo.valuation.web.entities.UploadResponse;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/upload")
public class UploadResource {

    private final TradeUploadService tradeUploadService;
    private final TradeCacheService cacheService;
    private final PortfolioManager portfolioManager;

    @Inject
    public UploadResource(TradeUploadService tradeUploadService,
                          TradeCacheService cacheService,
                          PortfolioManager portfolioManager) {
        this.tradeUploadService = tradeUploadService;
        this.cacheService = cacheService;
        this.portfolioManager = portfolioManager;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/v1")
    @Timed
    public Response uploadV1(MultipartFormDataInput input/*@MultipartForm UploadForm entity*/) throws IOException {
        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> parts = formDataMap.get("file");
        log.info("start uploading trade {} file(s)", parts.size());
        Set<Portfolio> portfolios = parts.stream()
                .map(part -> {
                    try {
                        return part.getBody(byte[].class, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(ByteArrayInputStream::new)
                .flatMap(fis -> tradeUploadService.fromExcelWithValues(fis).stream())
                .collect(toSet());
        LocalDate valuationDate = LocalDateUtils.valuationDate();
        final MarginCallResponse  response = portfolioManager.split(new ArrayList<>(portfolios), valuationDate);
        log.info("uploading trade file complete, loaded {} portfolios", portfolios.size());
        return Response.status(CREATED).entity(response).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response upload(@MultipartForm UploadForm entity) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream(entity.getFile());
        log.info("start uploading trade file ");
        List<String> trades = tradeUploadService.fromExcel(fis);
        final UploadResponse response = new UploadResponse();
        final UploadResponse.Status success = new UploadResponse.Status(UploadResponse.StatusType.success, trades.size() +" trades have been uploaded");
        final UploadResponse.Status failure = new UploadResponse.Status(UploadResponse.StatusType.failure, "no trade have failed to upload");
        response.setStatuses(ImmutableList.of(success, failure));
        String tnxId = cacheService.put(trades);
        response.setTxnID(tnxId);
        log.info("uploading trade file complete, loaded {} trades, txnId [{}]", trades.size(), tnxId);
        return Response.status(CREATED).entity(response).build();
    }
}
