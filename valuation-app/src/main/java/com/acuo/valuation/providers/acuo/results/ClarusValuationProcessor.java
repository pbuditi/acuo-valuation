package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.MarginCall;
import com.acuo.valuation.protocol.results.MarginResults;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class ClarusValuationProcessor {

    private final ResultProcessor<MarginResults> firstProcessor;

    @Inject
    public ClarusValuationProcessor(ResultProcessor<MarginResults> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    public List<MarginCall> process(MarginResults results) {
        log.info("starting pricing result processing");
        ProcessorItem<MarginResults> processorItem = firstProcessor.process(new ProcessorItem(results));
        log.info("pricing result processing ended");
        return processorItem.getExpected();
    }

}
