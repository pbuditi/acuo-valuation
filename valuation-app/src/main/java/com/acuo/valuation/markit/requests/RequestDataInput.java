package com.acuo.valuation.markit.requests;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.requests.RequestData;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestDataInput {

	public RequestDataInput() {
		swaps = new ArrayList<>();
	}

	RequestDataInput(RequestData data) {
		List<IrSwap> values = data.values(IrSwap.class);
		swaps = values.stream().map(swap -> new IrSwapInput(swap)).collect(Collectors.toList());
	}

	@XmlElement(name = "irswap")
	public List<IrSwapInput> swaps;
}