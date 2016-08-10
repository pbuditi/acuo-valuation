package com.acuo.valuation.services;

import com.acuo.valuation.protocol.results.Result;

import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;

public interface MarginCalcService {

    String send(String request);

    List<? extends Result> send(String data, DataFormat format, DataType type);
}
