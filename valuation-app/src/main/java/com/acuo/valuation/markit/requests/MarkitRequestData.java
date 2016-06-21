package com.acuo.valuation.markit.requests;

import static com.acuo.valuation.markit.requests.Key.key;

import com.acuo.valuation.markit.requests.swap.IrSwap;
import com.acuo.valuation.requests.RequestData;

public class MarkitRequestData implements RequestData {

	private MarkitRequestData(RequestDataInput definition) {
		definition.swaps.stream().map(swap -> new IrSwap(swap))
				.forEachOrdered(swap -> put(key(swap.tradeId(), IrSwap.class), swap));
	}

	public static RequestData of(RequestDataInput definition) {
		return new MarkitRequestData(definition);
	}
}
