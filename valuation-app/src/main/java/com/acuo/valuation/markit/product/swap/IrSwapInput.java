package com.acuo.valuation.markit.product.swap;

import com.acuo.common.marshal.LocalDateAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IrSwapInput {

    public IrSwapInput() {
    }

    public IrSwapInput(IrSwap swap) {
        tradeId = swap.tradeId();
        tradeDate = swap.tradeDate();
        book = swap.book();
        legs = swap.legs().stream().map(leg -> new IrSwapLegInput(leg))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @XmlPath("tradeid/text()")
    public String tradeId;

    @XmlPath("tradedate/text()")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate tradeDate;

    @XmlPath("book/text()")
    public String book;

    @XmlElement(name = "leg")
    public List<IrSwapLegInput> legs;
}