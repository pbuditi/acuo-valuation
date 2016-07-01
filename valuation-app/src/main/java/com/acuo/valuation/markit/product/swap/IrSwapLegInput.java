package com.acuo.valuation.markit.product.swap;

import com.acuo.common.marshal.DecimalAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class IrSwapLegInput {

    public IrSwapLegInput() {

    }

    public IrSwapLegInput(IrSwapLeg leg) {
        id = leg.id();
        currency = leg.currency();
        fixing = leg.fixing() != null ? new IrSwapLegFixingInput(leg.fixing()) : null;
        spread = leg.spread();
        rate = leg.rate();
        type = leg.type();
        daycount = leg.daycount();
        notional = leg.notional();
        notionalxg = leg.notionalxg();
        payDates = new IrSwapLegPayDatesInput(leg.payDates());
    }

    @XmlPath("id/text()")
    public int id;

    @XmlPath("currency/text()")
    public String currency;

    @XmlElement(name = "fixing")
    public IrSwapLegFixingInput fixing;

    @XmlPath("spread/text()")
    @XmlJavaTypeAdapter(DecimalAdapter.class)
    public Double spread;

    @XmlPath("rate/text()")
    @XmlJavaTypeAdapter(DecimalAdapter.class)
    public Double rate;

    @XmlPath("type/text()")
    public String type;

    @XmlPath("daycount/text()")
    public String daycount;

    @XmlPath("notional/text()")
    @XmlJavaTypeAdapter(DecimalAdapter.class)
    public Double notional;

    @XmlPath("notionalxg/text()")
    public String notionalxg;

    @XmlElement(name = "paydates")
    public IrSwapLegPayDatesInput payDates;
}