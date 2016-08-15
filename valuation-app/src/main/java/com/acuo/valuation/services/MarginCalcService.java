package com.acuo.valuation.services;

import com.acuo.common.model.IrSwap;
import com.acuo.valuation.protocol.results.Result;

import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;

public interface MarginCalcService {

    List<? extends Result> send(List<IrSwap> swaps, DataFormat format, DataType type);

}
