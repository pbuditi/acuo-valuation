package com.acuo.valuation.services;

import okhttp3.Request;

import java.util.function.Predicate;

public interface Call {

    Request getRequest();

    Predicate<String> getPredicate();

    String send();

}
