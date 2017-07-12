package com.acuo.valuation.providers.acuo.calls;

public interface CallGeneratorProcessor {

    CallProcessorItem process(CallProcessorItem callProcessorItem);

    void setNext(CallGeneratorProcessor callGeneratorProcessor);

}
