package com.acuo.valuation.providers.acuo.results;

public interface MarkitResultProcessor {

    MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem);

    void setNext(MarkitResultProcessor nextProcessor);
}
