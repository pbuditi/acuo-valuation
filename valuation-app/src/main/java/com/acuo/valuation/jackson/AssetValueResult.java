package com.acuo.valuation.jackson;

import com.acuo.persist.entity.AssetValue;
import com.opengamma.strata.basics.currency.Currency;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Slf4j
public class AssetValueResult {

    private Double yield;

    private Double unitValue;

    private LocalDateTime valuationDateTime;

    private String priceQuotationType;

    private Currency nominalCurrency;

    private Currency reportCurrency;

    private Double coupon;

    public AssetValueResult(AssetValue value) {
        this.yield = value.getYield();
        this.unitValue = value.getUnitValue();
        this.valuationDateTime = LocalDateTime.ofInstant(value.getTimestamp(), ZoneOffset.UTC);
        this.priceQuotationType = value.getPriceQuotationType();
        this.nominalCurrency = value.getNominalCurrency();
        this.reportCurrency = value.getReportCurrency();
        this.coupon = value.getCoupon();
    }

}
