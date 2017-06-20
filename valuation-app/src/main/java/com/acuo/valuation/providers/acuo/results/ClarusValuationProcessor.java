package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.services.MarginCallService;
import com.acuo.valuation.protocol.results.MarginResults;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class ClarusValuationProcessor {

    private final ResultProcessor<MarginResults> firstProcessor;
    private final MarginCallService marginCallService;

    @Inject
    public ClarusValuationProcessor(ResultProcessor<MarginResults> firstProcessor, MarginCallService marginCallService) {
        this.firstProcessor = firstProcessor;
        this.marginCallService = marginCallService;
    }

    public List<MarginCall> process(MarginResults results) {
        log.info("starting pricing result processing");
        ProcessorItem<MarginResults> processorItem = firstProcessor.process(new ProcessorItem(results));
        log.info("pricing result processing ended");
        final List<String> ids = processorItem.getExpected();
        return ids.stream().map(id -> marginCallService.find(id, 2)).collect(toList());
    }

}
