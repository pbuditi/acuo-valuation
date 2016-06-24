package com.acuo.valuation.markit.services;

import lombok.Data;
import okhttp3.Request;

import java.util.function.Predicate;

@Data
public class MarkitClientCall {

    private final Request request;
    private final Predicate<String> predicate;

    public MarkitClientCall(Request request, Predicate<String> predicate) {
        this.request = request;
        this.predicate = predicate;
    }
}