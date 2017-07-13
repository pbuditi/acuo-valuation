package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.ClientSignsRelation;
import com.acuo.persist.entity.CounterpartSignsRelation;
import com.acuo.persist.entity.LegalEntity;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.ValuationService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
        marginCallResult.setExposure(marginCall.getExposure());

        String type = agreement.getType();
        if ("bilateral".equals(type) || "legacy".equals(type)) {
            marginCallResult.agreementdetails.setPricingSource("Markit");
        } else {
            marginCallResult.agreementdetails.setPricingSource("Clarus");
        }

        return marginCallResult;
    }

    private static abstract class Builder {

        protected final Portfolio portfolio;
        protected final ValuationService valuationService;
        final Agreement agreement;
        protected final PortfolioId portfolioId;
        protected final String currency;

        Builder(Portfolio portfolio, ValuationService valuationService) {
            this.portfolio = portfolio;
            this.valuationService = valuationService;
            this.portfolioId = portfolio.getPortfolioId();
            this.currency = portfolio.getCurrency();
            this.agreement = portfolio.getAgreement();
        }

        protected MarginCallResult build() {
            MarginCallResult marginCallResult = new MarginCallResult();

            marginCallResult.marginagreement = agreement.getAgreementId();
            marginCallResult.currency = currency;
            marginCallResult.referenceidentifier = portfolioId.toString();

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

            return marginCallResult;
        }
    }

    static class BilateralBuilder extends Builder {

        BilateralBuilder(Portfolio portfolio, ValuationService valuationService) {
            super(portfolio, valuationService);
        }

        public MarginCallResult build() {
            MarginCallResult marginCallResult = super.build();
            marginCallResult.agreementdetails.setPricingSource("Markit");

            double totalPV = 0.0d;
            LocalDate valuationDate = LocalDate.now();
            marginCallResult.setValuationdate(valuationDate);
            Long totalTradeCount = valuationService.tradeCount(portfolioId);
            Long valuatedTradeCount = valuationService.tradeValuedCount(portfolioId, valuationDate);

            MarginValuation marginValuation = valuationService.getMarginValuationFor(portfolioId, Types.CallType.Variation);
            if (marginValuation != null && marginValuation.getValues() != null) {
                for (MarginValue marginValue : marginValuation.getValues()) {
                    if (marginValue.getValuationDate().equals(valuationDate)) {
                        totalPV = marginValue.getAmount();
                        break;
                    }
                }
            }

            marginCallResult.agreementdetails.setTradeCount(totalTradeCount);
            marginCallResult.agreementdetails.setTradeValue(valuatedTradeCount);
            marginCallResult.setExposure(totalPV);

            return marginCallResult;
        }

    }

    private static abstract class ClearedBuilder extends Builder {

        ClearedBuilder(Portfolio portfolio, ValuationService valuationService) {
            super(portfolio, valuationService);
        }

        protected abstract Types.CallType callType();

        public MarginCallResult build() {

            MarginCallResult marginCallResult = super.build();

            double totalPV = 0.0d;
            Long valuatedTradeCount = 0L;
            Long totalTradeCount = valuationService.tradeCount(portfolioId);
            LocalDate valuationDate = LocalDate.now();
            marginCallResult.setValuationdate(valuationDate);

            //check for Clarus valuation, if there is today margin valuation on the portfolio, we can assume the trades are valuated
            MarginValuation marginValuation = valuationService.getMarginValuationFor(portfolioId, callType());
            if (marginValuation != null && marginValuation.getValues() != null) {
                for (MarginValue marginValue : marginValuation.getValues()) {
                    if (marginValue.getValuationDate().equals(valuationDate)) {
                        valuatedTradeCount = totalTradeCount;
                        totalPV = marginValue.getAmount();
                        break;
                    }
                }
            }

            marginCallResult.agreementdetails.setTradeCount(totalTradeCount);
            marginCallResult.agreementdetails.setTradeValue(valuatedTradeCount);
            marginCallResult.setExposure(totalPV);

            return marginCallResult;
        }
    }

    static class ClearedVMBuilder extends ClearedBuilder {

        private final Types.CallType callType = Types.CallType.Variation;

        ClearedVMBuilder(Portfolio portfolio, ValuationService valuationService) {
            super(portfolio, valuationService);
        }

        @Override
        protected Types.CallType callType() {
            return callType;
        }

        public MarginCallResult build() {
            MarginCallResult marginCallResult = super.build();
            marginCallResult.agreementdetails.setPricingSource("Clarus");
            return marginCallResult;
        }
    }

    static class ClearedIMBuilder extends ClearedBuilder {

        private final Types.CallType callType = Types.CallType.Initial;

        ClearedIMBuilder(Portfolio portfolio, ValuationService valuationService) {
            super(portfolio, valuationService);
        }

        @Override
        protected Types.CallType callType() {
            return callType;
        }

        public MarginCallResult build() {
            MarginCallResult marginCallResult = super.build();
            marginCallResult.agreementdetails.setPricingSource("Clarus");
            return marginCallResult;
        }
    }
}