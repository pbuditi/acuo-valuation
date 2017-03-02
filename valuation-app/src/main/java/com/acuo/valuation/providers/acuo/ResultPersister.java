package com.acuo.valuation.providers.acuo;

public interface ResultPersister<T> {

    void persist(T results);
}
