package com.acuo.valuation.services;

import com.acuo.valuation.markit.services.MarkitClientCall;
import com.acuo.valuation.requests.RequestBuilder;

public interface ClientEndPoint {

    <T extends RequestBuilder<T>> T get();

    <T extends RequestBuilder<T>> T post();

    String send(MarkitClientCall call);
}
