package com.acuo.valuation.services;

import okhttp3.Request;

import java.util.function.Predicate;

public interface ClientEndPoint<T> {

    T config();

    Call call(Request request, Predicate<String> predicate);

    String send(Call call);
}
