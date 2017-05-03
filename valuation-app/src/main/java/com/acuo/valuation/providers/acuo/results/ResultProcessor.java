package com.acuo.valuation.providers.acuo.results;

public interface ResultProcessor<R> {

    ProcessorItem process(ProcessorItem<R> processorItem);

    void setNext(ResultProcessor<R> nextProcessor);
}
