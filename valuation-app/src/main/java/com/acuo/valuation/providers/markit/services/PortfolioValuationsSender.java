package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.providers.markit.product.swap.IrSwap;
import com.acuo.valuation.providers.markit.product.swap.IrSwapInput;
import com.acuo.valuation.providers.markit.protocol.reports.ReportParser;
import com.acuo.valuation.providers.markit.protocol.requests.MarkitRequestData;
import com.acuo.valuation.providers.markit.protocol.requests.RequestDataInput;
import com.acuo.valuation.providers.markit.protocol.requests.RequestInput;
import com.acuo.valuation.providers.markit.protocol.requests.RequestParser;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.requests.RequestData;
import com.acuo.valuation.services.ClientEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDate;

public class PortfolioValuationsSender implements Sender {

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioValuationsSender.class);
    private static final String STILL_PROCESSING_KEY = "Markit upload still processing.";

    private final ClientEndPoint client;
    private final RequestParser requestParser;
    private final ReportParser reportParser;

    @Inject
    public PortfolioValuationsSender(ClientEndPoint<MarkitEndPointConfig> client, RequestParser requestParser, ReportParser reportParser) {
        this.client = client;
        this.requestParser = requestParser;
        this.reportParser = reportParser;
    }

    public Report send(IrSwap swap) {
        try {
            String file = generateFile(swap);
            return send(file);
        } catch (Exception e) {
            LOG.error("error uploading file for {} to markit pv service", swap, e);
        }
        return null;
    }

    public Report send(String file) {
        try {
            String key = MarkitMultipartCall.of(client)
                                            .with("theFile", file)
                                            .create()
                                            .send();
            String report = MarkitFormCall.of(client)
                                          .with("key", key)
                                          .with("version", "2")
                                          .retryWhile(s -> s.startsWith(STILL_PROCESSING_KEY))
                                          .create()
                                          .send();
            if (LOG.isDebugEnabled()) LOG.debug(report);
            return reportParser.parse(report);
        } catch (Exception e) {
            LOG.error("error uploading file for {} to markit pv service", file, e);
        }
        return null;
    }

    private String generateFile(IrSwap swap) throws Exception {
        LocalDate valuationDate = LocalDate.now();
        String valuationCurrency = "USD";
        IrSwapInput swapInput = new IrSwapInput(swap);
        RequestDataInput dataInput = new RequestDataInput();
        dataInput.swaps.add(swapInput);
        RequestData data = MarkitRequestData.of(dataInput);
        RequestInput input = new RequestInput(valuationDate, valuationCurrency, data);
        return requestParser.parse(input.request());
    }
}