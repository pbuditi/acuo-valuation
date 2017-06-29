package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.acuo.persist.entity.*;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Iterator;

import static com.acuo.common.model.margin.Types.MarginType.Initial;
import static com.acuo.common.model.margin.Types.MarginType.Variation;

@lombok.Data
@Slf4j
public class MarginCallResult {

    @JsonProperty("legalEntity")
    private String legalentity;
    @JsonProperty("cptyOrg")
    private String cptyorg;
    @JsonProperty("cptyEntity")
    private String cptyentity;
    @JsonProperty("marginAgreement")
    private String marginagreement;
    @JsonProperty("valuationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private LocalDate valuationdate;
    @JsonProperty("callDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private LocalDate calldate;
    @JsonProperty("callType")
    private String calltype;
    private String currency;
    @JsonProperty("totalCallAmount")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double totalcallamount;
    @JsonProperty("referenceIdentifier")
    private String referenceidentifier;
    @JsonSerialize(using = DoubleSerializer.class)
    private Double exposure;
    @JsonProperty("collateralValue")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double collateralvalue;
    @JsonProperty("pendingCollateral")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double pendingcollateral;
    @JsonProperty("mgnCallUploadId")
    private String mgncalluploadid;
    @JsonProperty("agreementDetails")
    private Agreementdetails agreementdetails;

    public static MarginCallResult of(Portfolio portfolio, TradeService tradeService, ValuationService valuationService)
    {
        MarginCallResult marginCallResult = new MarginCallResult();
        Agreement agreement = portfolio.getAgreement();
        marginCallResult.marginagreement = agreement.getAgreementId();
        marginCallResult.currency = portfolio.getCurrency();
        marginCallResult.referenceidentifier = portfolio.getPortfolioId().toString();

        ClientSignsRelation r = agreement.getClientSignsRelation();
        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();
        if (counterpartSignsRelation != null) {
            LegalEntity legalEntity = counterpartSignsRelation.getLegalEntity();
            marginCallResult.cptyentity = legalEntity.getName();
            if (legalEntity.getFirm() != null) {
                marginCallResult.cptyorg = legalEntity.getFirm().getName();
            } else {
                log.debug("agreement[{}] le[{}] has firm set to null", agreement.getAgreementId(), legalEntity);
            }
        }
        if (r != null) {
            marginCallResult.legalentity = r.getLegalEntity().getName();
        }

        marginCallResult.agreementdetails = new Agreementdetails();
        marginCallResult.agreementdetails.setThreshold(agreement.getThreshold());
        if (r != null) {
            marginCallResult.agreementdetails.setMintransfer(r.getMTA());
            marginCallResult.agreementdetails.setRounding(r.getRounding());
            marginCallResult.agreementdetails.setThreshold(r.getThreshold());

        }

        double totalPV = 0;
        int totalCount = 0;
        int valudatedCount = 0;

        Iterable<Trade> iterable = tradeService.findByPortfolioId(portfolio.getPortfolioId());
        if(iterable !=  null)
        {
            Iterator<Trade> trades = iterable.iterator();
            while(trades.hasNext())
            {
                Trade trade = trades.next();
                totalCount++;
                TradeValuation tradeValuation = valuationService.getTradeValuationFor(trade.getTradeId());
                if(tradeValuation != null)
                {

                    for(TradeValue value : tradeValuation.getValues())
                    {
                        if(LocalDate.now().minusDays(2).isBefore(value.getValuationDate()))
                        {
                            valudatedCount++;
                            totalPV += value.getPv();
                            break;
                        }
                    }
                }
            }
        }

        marginCallResult.agreementdetails.setTradeCount(Long.valueOf(totalCount));
        marginCallResult.agreementdetails.setTradeValue(Long.valueOf(valudatedCount));
        marginCallResult.setExposure(totalPV);



        String type = agreement.getType();
        if ("bilateral".equals(type) || "legacy".equals(type)) {
            marginCallResult.agreementdetails.setPricingSource("Markit");
        } else {
            marginCallResult.agreementdetails.setPricingSource("Clarus");
        }

        return marginCallResult;
    }

    public static MarginCallResult of(MarginCall marginCall) {
        MarginCallResult marginCallResult = new MarginCallResult();
        Agreement agreement = marginCall.getMarginStatement().getAgreement();
        marginCallResult.marginagreement = agreement.getAgreementId();
        marginCallResult.valuationdate = marginCall.getValuationDate();
        marginCallResult.calldate = marginCall.getCallDate();
        marginCallResult.calltype = marginCall.getMarginType().name();
        marginCallResult.currency = marginCall.getCurrency().getCode();
        marginCallResult.totalcallamount = marginCall.getExcessAmount();
        marginCallResult.referenceidentifier = marginCall.getItemId();
        marginCallResult.exposure = marginCall.getExposure();
        marginCallResult.pendingcollateral = marginCall.getPendingCollateral();
        marginCallResult.totalcallamount = marginCall.getMarginAmount();
        ClientSignsRelation r = agreement.getClientSignsRelation();
        if (Variation.equals(marginCall.getMarginType()) && r != null)
            marginCallResult.collateralvalue = r.getVariationBalance();
        else if (Initial.equals(marginCall.getMarginType()) && r != null)
            marginCallResult.collateralvalue = r.getInitialBalance();

        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();
        if (counterpartSignsRelation != null) {
            LegalEntity legalEntity = counterpartSignsRelation.getLegalEntity();
            marginCallResult.cptyentity = legalEntity.getName();
            if (legalEntity.getFirm() != null) {
                marginCallResult.cptyorg = legalEntity.getFirm().getName();
            } else {
                log.debug("agreement[{}] le[{}] has firm set to null", agreement.getAgreementId(), legalEntity);
            }
        }
        if (r != null) {
            marginCallResult.legalentity = r.getLegalEntity().getName();
        }

        marginCallResult.agreementdetails = new Agreementdetails();
        marginCallResult.agreementdetails.setNetrequired(marginCall.getMarginAmount());
        marginCallResult.agreementdetails.setThreshold(agreement.getThreshold());
        if (r != null) {
            marginCallResult.agreementdetails.setMintransfer(r.getMTA());
            marginCallResult.agreementdetails.setRounding(r.getRounding());
            marginCallResult.agreementdetails.setThreshold(r.getThreshold());

        }

        marginCallResult.agreementdetails.setFxRate(marginCall.getFxRate());
        marginCallResult.agreementdetails.setTradeCount(marginCall.getTradeCount());
        marginCallResult.agreementdetails.setTradeValue(marginCall.getTradeCount());

        String type = agreement.getType();
        if ("bilateral".equals(type) || "legacy".equals(type)) {
            marginCallResult.agreementdetails.setPricingSource("Markit");
        } else {
            marginCallResult.agreementdetails.setPricingSource("Clarus");
        }

        return marginCallResult;
    }
}