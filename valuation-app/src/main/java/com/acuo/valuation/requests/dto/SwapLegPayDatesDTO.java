package com.acuo.valuation.requests.dto;

import com.acuo.common.marshal.LocalDateAdapter;
import lombok.Data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@Data
public class SwapLegPayDatesDTO {

    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate startdate;
    private String freq;
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate enddate;
    private String rollcode;
    private boolean adjust;
    private boolean eom;

}
