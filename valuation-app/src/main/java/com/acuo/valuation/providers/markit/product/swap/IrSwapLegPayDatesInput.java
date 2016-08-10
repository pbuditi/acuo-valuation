package com.acuo.valuation.providers.markit.product.swap;

import com.acuo.common.marshal.LocalDateAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

public class IrSwapLegPayDatesInput {

    public IrSwapLegPayDatesInput() {

    }

    public IrSwapLegPayDatesInput(IrSwapLegPayDates dates) {
        startDate = dates.startDate();
        frequency = dates.frequency();
        enddate = dates.enddate();
        rollCode = dates.rollCode();
        adjust = dates.isAdjust();
        eom = dates.isEom();
    }

    @XmlPath("startdate/text()")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate startDate;

    @XmlPath("freq/text()")
    public String frequency;

    @XmlPath("enddate/text()")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate enddate;

    @XmlPath("rollcode/text()")
    public String rollCode;

    @XmlPath("adjust/text()")
    public boolean adjust;

    @XmlPath("eom/text()")
    public boolean eom;
}