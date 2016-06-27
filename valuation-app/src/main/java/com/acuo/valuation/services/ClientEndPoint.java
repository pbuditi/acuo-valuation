package com.acuo.valuation.services;

import okhttp3.Request;

import java.util.function.Predicate;

public interface ClientEndPoint {

    EndPointConfig config();

    ClientCall call(Request request, Predicate<String> predicate);

    String send(ClientCall call);
}
