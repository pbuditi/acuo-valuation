package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.VariationMargin;
import com.acuo.valuation.protocol.results.MarkitResults;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class MarkitValuationProcessor {

    private final ResultProcessor<MarkitResults> firstProcessor;

    @Inject
    public MarkitValuationProcessor(ResultProcessor<MarkitResults> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    public List<VariationMargin> process(MarkitResults results) {
        log.info("starting pricing result processing");
        ProcessorItem<MarkitResults> processorItem = firstProcessor.process(new ProcessorItem(results));
        log.info("pricing result processing ended");
        return processorItem.getExpected();
    }

}
