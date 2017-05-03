package com.acuo.valuation.providers.acuo.results;

public abstract class AbstractResultProcessor<R> implements ResultProcessor<R> {

    protected ResultProcessor<R> next;

    public final void setNext(ResultProcessor<R> next) {
        this.next = next;
    }
}
