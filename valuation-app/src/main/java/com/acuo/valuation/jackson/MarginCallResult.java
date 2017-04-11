package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.ClientSignsRelation;
import com.acuo.persist.entity.CounterpartSignsRelation;
import com.acuo.persist.entity.LegalEntity;
import com.acuo.persist.entity.VariationMargin;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import static com.acuo.common.model.margin.Types.CallType.Initial;
import static com.acuo.common.model.margin.Types.CallType.Variation;

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


    public static MarginCallResult of(VariationMargin marginCall) {
        MarginCallResult marginCallResult = new MarginCallResult();
        Agreement agreement = marginCall.getMarginStatement().getAgreement();
        marginCallResult.marginagreement = agreement.getAgreementId();
        marginCallResult.valuationdate = marginCall.getValuationDate();
        marginCallResult.calldate = marginCall.getCallDate();
        marginCallResult.calltype = marginCall.getMarginType().name();
        marginCallResult.currency = marginCall.getCurrency().getCode();
        marginCallResult.totalcallamount = marginCall.getExcessAmount();
        marginCallResult.referenceidentifier = marginCall.getMarginCallId();
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
        if (r != null) {
            marginCallResult.agreementdetails.setMintransfer(r.getMTA());
            marginCallResult.agreementdetails.setRounding(r.getRounding());
            marginCallResult.agreementdetails.setThreshold(r.getThreshold());

        }
        marginCallResult.agreementdetails.setNetrequired(marginCall.getMarginAmount());

        return marginCallResult;
    }
}