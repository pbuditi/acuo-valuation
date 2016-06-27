package com.acuo.valuation.services;

import okhttp3.Request;

import java.util.function.Predicate;

public interface ClientCall {

    Request getRequest();

    Predicate<String> getPredicate();

    String send();

}
