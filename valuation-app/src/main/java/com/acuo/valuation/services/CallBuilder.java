package com.acuo.valuation.services;

import java.util.function.Predicate;

public abstract class CallBuilder<CHILD extends CallBuilder<CHILD>> {

    protected Predicate<String> predicate = s -> false;

    public abstract CHILD with(String key, String value);

    public CHILD retryWhile(Predicate<String> predicate) {
        this.predicate = predicate;
        return (CHILD) this;
    }

    public abstract Call create();

}
