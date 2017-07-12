package com.acuo.valuation.providers.acuo.calls;

public abstract class AbstractCallGeneratorProcessor implements CallGeneratorProcessor {

    protected CallGeneratorProcessor next;

    public final void setNext(CallGeneratorProcessor next) {
        this.next = next;
    }
}