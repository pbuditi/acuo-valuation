package com.acuo.valuation.protocol.responses;

import com.acuo.valuation.protocol.results.Value;

import java.util.List;

public interface Response {

    public Header header();

    public List<Value> values();
}
