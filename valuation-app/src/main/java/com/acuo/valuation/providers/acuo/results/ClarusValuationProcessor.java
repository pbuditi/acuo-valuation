package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarkitResults;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class ClarusValuationProcessor {

    private final ResultProcessor<MarginResults> firstProcessor;

    @Inject
    public ClarusValuationProcessor(ResultProcessor<MarginResults> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    public List<VariationMargin> process(MarginResults results) {
        log.info("starting pricing result processing");
        ProcessorItem<MarginResults> processorItem = firstProcessor.process(new ProcessorItem(results));
        log.info("pricing result processing ended");
        return processorItem.getExpected();
    }

}
