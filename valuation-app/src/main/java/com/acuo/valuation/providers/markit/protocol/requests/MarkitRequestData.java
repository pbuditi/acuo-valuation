package com.acuo.valuation.providers.markit.protocol.requests;

import com.acuo.valuation.providers.markit.product.swap.*;
import com.acuo.valuation.protocol.requests.RequestData;

import static com.acuo.valuation.providers.markit.protocol.requests.Key.key;

public class MarkitRequestData implements RequestData {

    private MarkitRequestData(RequestDataInput input) {
        input.swaps.stream().map(swap -> new IrSwap(swap))
                .forEachOrdered(swap -> put(key(swap.tradeId(), IrSwap.class), swap));
    }

    public static RequestData of(RequestDataInput definition) {
        return new MarkitRequestData(definition);
    }

}
