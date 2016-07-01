package com.acuo.valuation.markit.requests;

import com.acuo.valuation.markit.product.swap.*;
import com.acuo.valuation.requests.RequestData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.*;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.swap.*;

import static com.acuo.valuation.markit.requests.Key.key;

public class MarkitRequestData implements RequestData {

    private MarkitRequestData(RequestDataInput input) {
        input.swaps.stream().map(swap -> new IrSwap(swap))
                .forEachOrdered(swap -> put(key(swap.tradeId(), IrSwap.class), swap));
    }

    public static RequestData of(RequestDataInput definition) {
        return new MarkitRequestData(definition);
    }

}
