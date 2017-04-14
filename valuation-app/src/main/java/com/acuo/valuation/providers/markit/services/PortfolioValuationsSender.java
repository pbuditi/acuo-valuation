package com.acuo.valuation.providers.markit.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.collateral.transform.services.MarkitTransformer;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.ArgChecker;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.List;

@Slf4j
public class PortfolioValuationsSender implements Sender {

    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    private final ClientEndPoint<MarkitEndPointConfig> client;
    private final Transformer<SwapTrade> transformer;
    private final ReportParser reportParser;

    @Inject
    public PortfolioValuationsSender(ClientEndPoint<MarkitEndPointConfig> client, ReportParser reportParser, @Named("markit") Transformer<SwapTrade> transformer) {
        ArgChecker.notNull(client, "client");
        ArgChecker.notNull(reportParser, "reportParser");
        ArgChecker.notNull(transformer, "trasnformer");
        ArgChecker.isTrue(transformer instanceof MarkitTransformer, "not markit transformer");
        this.client = client;
        this.reportParser = reportParser;
        this.transformer = transformer;
    }

    public Report send(List<SwapTrade> swaps) {
        log.info("sending {} trades for valuation", swaps.size());
        try {
            String file = generateFile(swaps);
            if (log.isDebugEnabled()) log.debug(file);
            return send(file);
        } catch (Exception e) {
            log.error("error uploading file for {} to markit pv service", swaps, e);
        }
        return null;
    }

    public Report send(String file) {
        try {
            log.info("uploading the file to markit PV");
            String key = MarkitMultipartCall.of(client)
                                            .with("theFile", file)
                                            .create()
                                            .send();
            log.info("retrieving the report with the key {}",key);
            String report = MarkitFormCall.of(client)
                                          .with("key", key)
                                          .with("version", "2")
                                          .retryWhile(s -> s.startsWith(STILL_PROCESSING_KEY))
                                          .create()
                                          .send();
            log.info("parsing the report");
            if (log.isDebugEnabled()) log.debug(report);
            return reportParser.parse(report);
        } catch (Exception e) {
            log.error("error uploading file for {} to markit pv service", file, e);
        }
        return null;
    }

    private String generateFile(List<SwapTrade> swaps) throws Exception {
        log.info("generating valuation request with for {} trades",swaps.size());
        LocalDate valuationDate = LocalDate.now();
        valuationDate = LocalDateUtils.minus(valuationDate, 1);
        log.info("with valuation date set to {}",valuationDate);
        TransformerContext context = new TransformerContext();
        context.setValueDate(valuationDate);
        return transformer.serialise(swaps, context);
    }
}