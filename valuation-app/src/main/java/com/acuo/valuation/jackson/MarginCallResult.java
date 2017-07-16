package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.ClientSignsRelation;
import com.acuo.persist.entity.CounterpartSignsRelation;
import com.acuo.persist.entity.LegalEntity;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Portfolio;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import static com.acuo.common.model.margin.Types.MarginType.Initial;
import static com.acuo.common.model.margin.Types.MarginType.Variation;
import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Slf4j
public class MarginCallResult {

    public String legalEntity;
    public String cptyOrg;
    public String cptyEntity;
    public String marginAgreement;
    public String callType;
    public String currency;
    public String referenceIdentifier;
    public String mgnCallUploadId;

    @JsonFormat(shape = STRING, pattern = "yyyy/MM/dd")
    public LocalDate valuationDate;

    @JsonFormat(shape = STRING, pattern = "yyyy/MM/dd")
    public LocalDate callDate;

    @JsonSerialize(using = DoubleSerializer.class)
    public Double totalCallAmount;

    @JsonSerialize(using = DoubleSerializer.class)
    public Double exposure;

    @JsonSerialize(using = DoubleSerializer.class)
    public Double collateralValue;

    @JsonSerialize(using = DoubleSerializer.class)
    public Double pendingCollateral;

    public AgreementDetails agreementDetails;

    public static MarginCallResult of(MarginCall marginCall) {
        return new Builder(marginCall).build();
    }

    public static MarginCallResult of(Portfolio portfolio,
                                      LocalDate valuationDate,
                                      String callType,
                                      String pricingSource,
                                      Long totalTradeCount,
                                      Long valuatedTradeCount,
                                      double totalPV) {
        return new Builder(portfolio, valuationDate, callType, pricingSource, totalTradeCount, valuatedTradeCount, totalPV).build();
    }

    @Slf4j
    private static class Builder {

        private final Agreement agreement;
        private final LocalDate valuationDate;
        private final String currency;
        private final Double totalPV;
        private final String referenceIdentifier;
        private final String callType;

        private final Long totalTradeCount;
        private final Long valuatedTradeCount;
        private final String pricingSource;

        private final LocalDate callDate;
        private final Double totalCallAmount;
        private final Double pendingCollateral;
        private final Double collateralValue;
        private final Double rate;
        private final Double netRequired;

        public Builder(MarginCall marginCall) {
            this.agreement = marginCall.getMarginStatement().getAgreement();
            this.valuationDate = marginCall.getValuationDate();
            this.currency = marginCall.getCurrency().getCode();
            this.totalPV = marginCall.getExposure();
            this.referenceIdentifier = marginCall.getItemId();
            this.callType = marginCall.getMarginType().name();

            this.totalTradeCount = marginCall.getTradeCount();
            this.valuatedTradeCount = marginCall.getTradeCount();
            String type = this.agreement.getType();
            if ("bilateral".equals(type) || "legacy".equals(type)) {
                this.pricingSource = "Markit";
            } else {
                this.pricingSource = "Clarus";
            }

            this.callDate = marginCall.getCallDate();
            this.totalCallAmount = marginCall.getMarginAmount();
            this.pendingCollateral = marginCall.getPendingCollateral();

            ClientSignsRelation clientSignsRelation = this.agreement.getClientSignsRelation();
            if (Variation.equals(marginCall.getMarginType()) && clientSignsRelation != null)
                this.collateralValue = clientSignsRelation.getVariationBalance();
            else if (Initial.equals(marginCall.getMarginType()) && clientSignsRelation != null)
                this.collateralValue = clientSignsRelation.getInitialBalance();
            else
                this.collateralValue = null;

            this.rate = marginCall.getFxRate();
            this.netRequired = totalCallAmount;
        }

        public Builder(Portfolio portfolio,
                LocalDate valuationDate,
                String callType,
                String pricingSource,
                Long totalTradeCount,
                Long valuatedTradeCount,
                double totalPV) {
            this.agreement = portfolio.getAgreement();
            this.valuationDate = valuationDate;
            this.currency = portfolio.getCurrency();
            this.totalPV = totalPV;
            this.referenceIdentifier = portfolio.getPortfolioId().toString();
            this.callType = callType;

            this.totalTradeCount = totalTradeCount;
            this.valuatedTradeCount = valuatedTradeCount;
            this.pricingSource = pricingSource;

            this.callDate = null;
            this.totalCallAmount = null;
            this.pendingCollateral = null;
            this.collateralValue = null;

            this.rate = null;
            this.netRequired = null;
        }

        public MarginCallResult build() {
            MarginCallResult marginCallResult = new MarginCallResult();
            marginCallResult.marginAgreement = this.agreement.getAgreementId();
            marginCallResult.valuationDate = this.valuationDate;
            marginCallResult.currency = this.currency;
            marginCallResult.exposure = this.totalPV;
            marginCallResult.referenceIdentifier = this.referenceIdentifier;
            marginCallResult.callType = this.callType;

            marginCallResult.callDate = this.callDate;
            marginCallResult.totalCallAmount = this.totalCallAmount;
            marginCallResult.pendingCollateral = this.pendingCollateral;
            marginCallResult.collateralValue = this.collateralValue;

            ClientSignsRelation clientSignsRelation = this.agreement.getClientSignsRelation();
            if (clientSignsRelation != null) {
                marginCallResult.legalEntity = clientSignsRelation.getLegalEntity().getName();
            }

            CounterpartSignsRelation counterpartSignsRelation = this.agreement.getCounterpartSignsRelation();
            if (counterpartSignsRelation != null) {
                LegalEntity legalEntity = counterpartSignsRelation.getLegalEntity();
                marginCallResult.cptyEntity = legalEntity.getName();
                if (legalEntity.getFirm() != null) {
                    marginCallResult.cptyOrg = legalEntity.getFirm().getName();
                } else {
                    log.debug("agreement[{}] le[{}] has firm set to null", agreement.getAgreementId(), legalEntity);
                }
            }

            marginCallResult.agreementDetails = new AgreementDetails();
            marginCallResult.agreementDetails.setTradeCount(this.totalTradeCount);
            marginCallResult.agreementDetails.setTradeValue(this.valuatedTradeCount);
            marginCallResult.agreementDetails.setPricingSource(this.pricingSource);

            marginCallResult.agreementDetails.setRate(this.rate);
            marginCallResult.agreementDetails.setNetRequired(this.netRequired);

            marginCallResult.agreementDetails.setThreshold(this.agreement.getThreshold());
            if (clientSignsRelation != null) {
                marginCallResult.agreementDetails.setMinTransfer(clientSignsRelation.getMTA());
                marginCallResult.agreementDetails.setRounding(clientSignsRelation.getRounding());
                marginCallResult.agreementDetails.setThreshold(clientSignsRelation.getThreshold());
            }

            return marginCallResult;
        }
    }
}