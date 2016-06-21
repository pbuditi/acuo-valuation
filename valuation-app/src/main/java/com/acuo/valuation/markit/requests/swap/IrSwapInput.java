package com.acuo.valuation.markit.requests.swap;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.acuo.common.marshal.jaxb.DateAdapter;

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
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date tradeDate;

	@XmlPath("book/text()")
	public String book;

	@XmlElement(name = "leg")
	public List<IrSwapLegInput> legs;
}