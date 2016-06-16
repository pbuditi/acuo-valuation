package com.acuo.valuation.requests.markit;

import static com.acuo.valuation.requests.markit.Key.key;

import com.acuo.valuation.requests.RequestData;
import com.acuo.valuation.requests.markit.swap.IrSwap;

public class MarkitRequestData implements RequestData {

	private MarkitRequestData(RequestDataInput definition) {
		definition.swaps.stream().map(swap -> new IrSwap(swap))
				.forEachOrdered(swap -> put(key(swap.tradeId(), IrSwap.class), swap));
	}

	public static RequestData of(RequestDataInput definition) {
		return new MarkitRequestData(definition);
	}
}
