package com.acuo.valuation.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.results.MarginResults;

import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;

public interface MarginCalcService {

    MarginResults send(List<SwapTrade> swaps, DataFormat format, DataType type);

}
