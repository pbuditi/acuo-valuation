package com.acuo.valuation.protocol.requests.dto;

import com.acuo.common.marshal.LocalDateAdapter;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.schedule.Frequency;
import lombok.Data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@Data
public class SwapLegPayDatesDTO {

    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startdate;
    private Frequency freq;
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate enddate;
    private BusinessDayConvention rollcode;
    private boolean adjust;
    private boolean eom;

}
