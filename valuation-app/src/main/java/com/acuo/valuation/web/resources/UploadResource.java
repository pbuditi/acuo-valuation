package com.acuo.valuation.web.resources;

import com.acuo.collateral.transform.Transformer;
import com.acuo.common.model.results.TradeValuation;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.jackson.MarginCallResponse;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.acuo.valuation.web.entities.UploadForm;
import com.acuo.valuation.web.entities.UploadResponse;
import com.codahale.metrics.annotation.Timed;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.parboiled.common.ImmutableList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/upload")
public class UploadResource {

    private final TradeUploadService tradeUploadService;
    private final TradeCacheService cacheService;
    private final Transformer<TradeValuation> transformer;
    private final ResultPersister<PortfolioResults> persister;
    private final PortfolioService portfolioService;
    private final TradeService<Trade> tradeService;
    private final ValuationService valuationService;

    @Inject
    public UploadResource(TradeUploadService tradeUploadService,
                          TradeCacheService cacheService,
                          @Named("tradeValuation") Transformer<TradeValuation> transformer,
                          ResultPersister<PortfolioResults> persister,
                          PortfolioService portfolioService,
                          TradeService<Trade> tradeService,
                          ValuationService valuationService) {
        this.tradeUploadService = tradeUploadService;
        this.cacheService = cacheService;
        this.transformer = transformer;
        this.persister = persister;
        this.portfolioService = portfolioService;
        this.tradeService = tradeService;
        this.valuationService = valuationService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/v1")
    @Timed
    public Response uploadV1(@MultipartForm UploadForm entity) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream(entity.getFile());
        log.info("start uploading trade file ");
        List<String> tradeIds = tradeUploadService.fromExcel(fis);
        Iterator<Trade> trades = tradeService.findAllTradeByIds(tradeIds.stream().map(TradeId::fromString).collect(Collectors.toList())).iterator();
        Set<PortfolioId> portfolios = new HashSet<>();
        while(trades.hasNext())
        {
            Trade trade = trades.next();
            if(trade != null && trade.getPortfolio() != null)
                portfolios.add(trade.getPortfolio().getPortfolioId());
        }
        List<TradeValuation> tradeValuations = transformer.deserialise(IOUtils.toByteArray(fis));
        PortfolioResults results = new PortfolioResults();
        results.setResults(tradeValuations.stream().map(Result::success).collect(Collectors.toList()));
        results.setCurrency(Currency.USD);
        results.setValuationDate(LocalDate.now());
        persister.persist(results);
        final MarginCallResponse  response = MarginCallResponse.ofPortfolio(portfolios.stream().map(id -> portfolioService.find(id, 2)).collect(Collectors.toList()), tradeService, valuationService);
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
        log.info("uploading trade file complete, txnId [{}]", tnxId);
        return Response.status(CREATED).entity(response).build();
    }
}
