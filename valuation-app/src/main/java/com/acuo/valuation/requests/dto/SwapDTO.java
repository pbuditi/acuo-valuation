package com.acuo.valuation.requests.dto;

import com.acuo.common.marshal.LocalDateAdapter;
import lombok.Data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;

@Data
public class SwapDTO {

    private String tradeId;
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate tradeDate;
    private String book;
    private List<SwapLegDTO> legs;
}
