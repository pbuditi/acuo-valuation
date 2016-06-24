package com.acuo.valuation.requests;

import java.util.function.Predicate;

public abstract class RequestBuilder<CHILD extends RequestBuilder<CHILD>> {

    protected Predicate<String> predicate = s -> false;

    public abstract CHILD with(String key, String value);

    public CHILD retryUntil(Predicate<String> predicate) {
        this.predicate = predicate;
        return (CHILD) this;
    }

    public abstract String send();

}
