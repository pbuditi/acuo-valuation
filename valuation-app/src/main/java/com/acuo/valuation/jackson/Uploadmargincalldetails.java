package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.acuo.common.model.margin.Types;
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

@lombok.Data
@Slf4j
public class Uploadmargincalldetails {

    @JsonProperty("legalEntity")
    private String legalentity;
    @JsonProperty("cptyOrg")
    private String cptyorg;
    @JsonProperty("cptyEntity")
    private String cptyentity;
    @JsonProperty("marginAgreement")
    private String marginagreement;
    @JsonProperty("valuationDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "CET")
    private LocalDate valuationdate;
    @JsonProperty("callDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "CET")
    private LocalDate calldate;
    @JsonProperty("callType")
    private String calltype;
    private String currency;
    @JsonProperty("totalCallAmount")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double totalcallamount;
    @JsonProperty("referenceIdentifier")
    private String referenceidentifier;
    private Double exposure;
    @JsonProperty("collateralValue")
    private Double collateralvalue;
    @JsonProperty("pendingCollateral")
    private Double pendingcollateral;
    @JsonProperty("mgnCallUploadId")
    private String mgncalluploadid;
    @JsonProperty("agreementDetails")
    private Agreementdetails agreementdetails;


    public static Uploadmargincalldetails of(VariationMargin marginCall) {
        Uploadmargincalldetails uploadmargincalldetails = new Uploadmargincalldetails();
        Agreement agreement = marginCall.getMarginStatement().getAgreement();
        uploadmargincalldetails.marginagreement = agreement.getAgreementId();
        uploadmargincalldetails.valuationdate = marginCall.getValuationDate();
        uploadmargincalldetails.calldate = marginCall.getCallDate();
        uploadmargincalldetails.calltype = marginCall.getMarginType().name();
        uploadmargincalldetails.currency = marginCall.getCurrency().getCode();
        uploadmargincalldetails.totalcallamount = marginCall.getExcessAmount();
        uploadmargincalldetails.referenceidentifier = marginCall.getMarginCallId();
        uploadmargincalldetails.exposure = marginCall.getExposure();
        uploadmargincalldetails.pendingcollateral = marginCall.getPendingCollateral();
        uploadmargincalldetails.totalcallamount = marginCall.getMarginAmount();
        ClientSignsRelation r = agreement.getClientSignsRelation();
        if (marginCall.getMarginType().equals(Types.CallType.Variation) && r != null)
            uploadmargincalldetails.collateralvalue = r.getVariationBalance();
        else if (marginCall.getMarginType().equals(Types.CallType.Initial) && r != null)
            uploadmargincalldetails.collateralvalue = r.getInitialBalance();

        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();
        if (counterpartSignsRelation != null) {
            LegalEntity legalEntity = counterpartSignsRelation.getLegalEntity();
            uploadmargincalldetails.cptyentity = legalEntity.getName();
            if (legalEntity.getFirm() != null) {
                uploadmargincalldetails.cptyorg = legalEntity.getFirm().getName();
            } else {
                log.debug("agreement[{}] le[{}] has firm set to null", agreement.getAgreementId(), legalEntity);
            }
        }
        if (r != null) {
            uploadmargincalldetails.legalentity = r.getLegalEntity().getName();
        }

        uploadmargincalldetails.agreementdetails = new Agreementdetails();
        if (r != null) {
            uploadmargincalldetails.agreementdetails.setMintransfer(r.getMTA());
            uploadmargincalldetails.agreementdetails.setRounding(r.getRounding());
            uploadmargincalldetails.agreementdetails.setThreshold(r.getThreshold());

        }
        uploadmargincalldetails.agreementdetails.setNetrequired(marginCall.getMarginAmount());

        return uploadmargincalldetails;
    }

}