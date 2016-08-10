package com.acuo.valuation.clarus.services;

import com.acuo.valuation.clarus.protocol.*;
import com.acuo.valuation.markit.product.swap.IrSwap;
import com.acuo.valuation.results.MarginResult;
import com.acuo.valuation.results.Result;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.MarginCalcService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.acuo.valuation.clarus.protocol.Clarus.*;
import static com.acuo.valuation.clarus.protocol.Clarus.DataType;
import static com.acuo.valuation.clarus.protocol.Clarus.MarginMethodology;

public class ClarusMarginCalcService implements MarginCalcService {

    private static final Logger LOG = LoggerFactory.getLogger(ClarusMarginCalcService.class);

    private static String csv = "Value Date,Position Account ID,Cleared Trade ID,Platform ID,Client ID,CME Swap Indicator,Currency,Effective Date,Maturity Date,Cleared Date,Status,Notional,Direction,Fixed Rate,Floating Index,NPV,Previous NPV,NPV Adjusted,Previous NPV Adjusted,Variation,PAI,PAI Rate,Upfront Payment,Fix Coupon Payment,Float Coupon Payment,Net CashFlow,Accrued Fixed Coupon,Accrued Float Coupon,Dirty Price,Clean Price,Firm ID,Source,Floating Rate,MARGIN_PRODUCT_CODE,LEG1_TYPE,LEG1_CCY,LEG1_START_DATE_ADJ_BUS_DAY_CONV,LEG1_MAT_DATE_ADJ_BUS_DAY_CONV,LEG1_MAT_DATE_ADJ_CAL,LEG1_PAY_FREQ,LEG1_PAY_REL_TO,LEG1_PAY_ADJ_BUS_DAY_CONV,LEG1_PAY_ADJ_CAL,LEG1_DAYCOUNT,LEG1_CALC_PER_ADJ_BUS_DAY_CONV,LEG1_CALC_PER_ADJ_CAL,LEG1_CALC_FREQ,LEG1_COMP_METHOD,LEG1_INDEX,LEG1_INDEX_TENOR,LEG1_RESET_FREQ,LEG1_RESET_REL_TO,LEG1_RESET_DATE_ADJ_BUS_DAY_CONV,LEG1_RESET_DATE_ADJ_CAL,LEG1_FIXING_DATE_OFFSET,LEG1_FIXING_DAY_TYPE,LEG1_FIXING_DATE_BUS_DAY_CONV,LEG1_FIXING_DATE_CAL,LEG1_START_DATE,LEG1_MAT_DATE,LEG1_NOTIONAL,LEG1_FIXED_RATE,LEG1_ROLL_CONV,LEG1_SPREAD,LEG1_STUB_TYPE,LEG1_FIRST_REG_PER_START_DATE,LEG1_LAST_REG_PER_END_DATE,LEG1_INITIAL_STUB_RATE,LEG1_INITIAL_STUBRATE_INDEX1,LEG1_INITIAL_STUBRATE_INDEX2,LEG1_FINAL_STUBRATE_INDEX1,LEG1_FINAL_STUBRATE_INDEX2,LEG1_CURRENT_PERIOD_RATE,LEG1_ACCRUED_INT,LEG1_INITIAL_STUB_INT_RATE,LEG1_FINAL_STUB_INT_RATE,LEG2_TYPE,LEG2_CCY,LEG2_START_DATE_ADJ_BUS_DAY_CONV,LEG2_MAT_DATE_ADJ_BUS_DAY_CONV,LEG2_MAT_DATE_ADJ_CAL,LEG2_PAY_FREQ,LEG2_PAY_REL_TO,LEG2_PAY_ADJ_BUS_DAY_CONV,LEG2_PAY_ADJ_CAL,LEG2_DAYCOUNT,LEG2_CALC_PER_ADJ_BUS_DAY_CONV,LEG2_CALC_PER_ADJ_CAL,LEG2_CALC_FREQ,LEG2_COMP_METHOD,LEG2_INDEX,LEG2_INDEX_TENOR,LEG2_RESET_FREQ,LEG2_RESET_REL_TO,LEG2_RESET_DATE_ADJ_BUS_DAY_CONV,LEG2_RESET_DATE_ADJ_CAL,LEG2_FIXING_DATE_OFFSET,LEG2_FIXING_DAY_TYPE,LEG2_FIXING_DATE_BUS_DAY_CONV,LEG2_FIXING_DATE_CAL,LEG2_START_DATE,LEG2_MAT_DATE,LEG2_NOTIONAL,LEG2_FIXED_RATE,LEG2_ROLL_CONV,LEG2_SPREAD,LEG2_STUB_TYPE,LEG2_FIRST_REG_PER_START_DATE,LEG2_LAST_REG_PER_END_DATE,LEG2_INITIAL_STUB_RATE,LEG2_INITIAL_STUBRATE_INDEX1,LEG2_INITIAL_STUBRATE_INDEX2,LEG2_FINAL_STUBRATE_INDEX1,LEG2_FINAL_STUBRATE_INDEX2,LEG2_CURRENT_PERIOD_RATE,LEG2_ACCRUED_INT,LEG2_INITIAL_STUB_INT_RATE,LEG2_FINAL_STUB_INT_RATE,LEG1_INITIAL_RATE,LEG2_INITIAL_RATE,DV01,LEG1_NEXT_ACCRUED_INT,LEG2_NEXT_ACCRUED_INT,NEXT_CLEAN_PRICE,BLOCK_TRADE_ID,ORIGIN,PRODUCT_TYPE,PV_FEES,PAYMENT_DATE,LEG1_NPV,LEG2_NPV,CLEARED_CALENDAR_DATE,FEE_TYPE,REG_TRADE_ID,LEG1_DIRECTION,LEG2_DIRECTION,LEG1_COUPON_AMT,LEG2_COUPON_AMT,DISCOUNT_METHODSTRICTLY CLIENT CONFIDENTIAL8 of 21 ,FRA_PAYMENT_DATE,LEG1_CURRENT_NOTIONAL,LEG1_NOTIONAL_TYPE,LEG2_CURRENT_NOTIONAL,LEG2_NOTIONAL_TYPE,KNOWN_AMOUNT,FEE_AMOUNT,FEE_PAYMENT_DATE,ORIGINATING_EVENT,TERMINATING_EVENT,LEG1_RATE_CUTOFF_DAYS_OFFSET,LEG2_RATE_CUTOFF_DAYS_OFFSET,LEG1_RATE_CUTOFF_DAY_TYPE,LEG2_RATE_CUTOFF_DAY_TYPE,LEG1_AVERAGING_METHOD,LEG2_AVERAGING_METHOD,TRADE_TYPE,VENUE_TYPE,SEF_LEI,LEG1_INITIAL_FIXING_DATE_OFFSET,LEG2_INITIAL_FIXING_DATE_OFFSET,LEG1_INITIAL_FIXING_DAY_TYPE,LEG2_INITIAL_FIXING_DAY_TYPE,LEG1_INITIAL_FIXING_DATE_BUS_DAY_CONV,LEG2_INITIAL_FIXING_DATE_BUS_DAY_CONV,LEG1_INITIAL_FIXING_DATE_CAL,LEG2_INITIAL_FIXING_DATE_CAL,FRA_FIXING_DATE,NETTING_ID,RISK_APPROVAL_ID,UTI,EXEC_TIME,BLENDING_ID\\n07/21/2014,1H,1,p1,cid1,,USD,10/03/2023,10/03/2033,10/02/2013,CLEARED,1000000,P,2.792,3M LIBOR,,,,,,,,,,,,,,,,123,MARKIT_WIRE,0.002318,VU3L3M3M6M,FIXED,USD,NONE,MODFOLLOWING,USNY,6M,END_PER,MODFOLLOWING,USNY,30/360,MODFOLLOWING,USNY,6M,None,,,,,,,,,,,10/03/2023,10/03/2033,1000000,0.02792,3,,NONE,,,,,,,,,,,,FLOAT,USD,NONE,MODFOLLOWING,USNY,3M,END_PER,MODFOLLOWING,USNY,ACT/360,MODFOLLOWING,USNY,3M,None,USD\\u00adLIBOR\\u00adBBA,3M,3M,BEG_PER,MODFOLLOWING,USNY,\\u00ad2D,Business,PRECEDING,GBLO,10/03/2023,10/03/2033,1000000,,3,,NONE,,,,,,,,0.002318,,,,,,,,,,,HOUS,SWAP,,07/22/2014,,,03/22/2013,,CCCIRS1,P,R,,,,,1000000,Bullet,1000000,Bullet,,,,NEW_TRADE,,,,,,,,,,,,,,,,,,,,,,,,";

    private final ClientEndPoint clientEndPoint;
    private final ObjectMapper mapper;

    @Inject
    public ClarusMarginCalcService(ClientEndPoint<ClarusEndPointConfig> clientEndPoint, ObjectMapper mapper) {
        this.clientEndPoint = clientEndPoint;
        this.mapper = mapper;
    }

    @Override
    public String send(String request) {
        return ClarusCall.of(clientEndPoint)
                .with("data", request)
                .create()
                .send();
    }

    @Override
    public List<? extends Result> send(String csv, DataFormat format, DataType type) {
        try {
            String request = buildRequest(csv, DataFormat.CME, DataType.SwapRegister);
            LOG.debug(request);
            String response = makeCall(request);
            LOG.debug(response);
            return buildResult(response);
        } catch (IOException e) {
            //TODO return an ErrorResult here instead of throwing an exception
            throw new RuntimeException("an error occurred while calculating margin: " + e.getMessage(), e);
        }
    }

    private String buildRequest(String data, DataFormat format, DataType type) throws JsonProcessingException {
        PortfolioData portfolioData = PortfolioDataBuilder
                .create()
                .addData(data)
                .addFormat(format)
                .addType(type)
                .build();
        RequestBuilder requestBuilder = RequestBuilder
                .create(LocalDate.now(), MarginMethodology.CME)
                .portfolioData(portfolioData);
        return mapper.writeValueAsString(requestBuilder.build());
    }

    private String makeCall(String request) {
        return ClarusCall.of(clientEndPoint)
                        .with("data", request)
                        .create()
                        .send();
    }

    private List<MarginResult> buildResult(String response) throws IOException {
        Response res = mapper.readValue(response, Response.class);
        return res.getResults().entrySet().stream().map(map -> new MarginResult(map.getKey(),
                map.getValue().get("Account"),
                map.getValue().get("Change"),
                map.getValue().get("Margin")))
                .collect(Collectors.toList());
    }
}