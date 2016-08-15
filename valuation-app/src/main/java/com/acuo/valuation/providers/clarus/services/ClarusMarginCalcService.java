package com.acuo.valuation.providers.clarus.services;

import com.acuo.collateral.transform.services.DataMapper;
import com.acuo.common.model.IrSwap;
import com.acuo.valuation.protocol.results.MarginResult;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.providers.clarus.protocol.*;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.MarginCalcService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.*;

public class ClarusMarginCalcService implements MarginCalcService {

    private static final Logger LOG = LoggerFactory.getLogger(ClarusMarginCalcService.class);

    private final ClientEndPoint clientEndPoint;
    private final ObjectMapper objectMapper;
    private final DataMapper dataMapper;

    @Inject
    public ClarusMarginCalcService(ClientEndPoint<ClarusEndPointConfig> clientEndPoint, ObjectMapper objectMapper, DataMapper dataMapper) {
        this.clientEndPoint = clientEndPoint;
        this.objectMapper = objectMapper;
        this.dataMapper = dataMapper;
    }

    @Override
    public List<? extends Result> send(List<IrSwap> swaps, DataFormat format, DataType type) {
        try {
            String request = makeRequest(swaps, format, type);
            String response = sendRequest(request);
            return makeResult(response);
        } catch (IOException e) {
            //TODO return an ErrorResult here instead of throwing an exception
            throw new RuntimeException("an error occurred while calculating margin: " + e.getMessage(), e);
        }
    }

    String makeRequest(List<IrSwap> swaps, DataFormat format, DataType type) {
        String data = dataMapper.toCmeFile(swaps,LocalDate.now());
        String request = RequestBuilder
                .create(objectMapper)
                .addData(data)
                .addFormat(format)
                .addType(type)
                .marginMethodology(MarginMethodology.CME)
                .build();
        LOG.debug(request);
        return request;
    }

    String sendRequest(String request) {
        String response = ClarusCall.of(clientEndPoint)
                .with("data", request)
                .create()
                .send();
        LOG.debug(response);
        return response;
    }

    List<MarginResult> makeResult(String response) throws IOException {
        Response res = objectMapper.readValue(response, Response.class);
        return res.getResults().entrySet().stream().map(map -> new MarginResult(map.getKey(),
                map.getValue().get("Account"),
                map.getValue().get("Change"),
                map.getValue().get("Margin")))
                .collect(Collectors.toList());
    }
}