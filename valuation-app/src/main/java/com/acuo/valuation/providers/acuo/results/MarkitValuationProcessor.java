package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.PricingResults;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class MarkitValuationProcessor {

    private final PricingResultProcessor firstProcessor;

    @Inject
    public MarkitValuationProcessor(PricingResultProcessor firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    public List<VariationMargin> process(PricingResults results) {
        log.info("starting pricing result processing");
        ProcessorItem processorItem = firstProcessor.process(new ProcessorItem(results));
        log.info("pricing result processing ended");
        return processorItem.getExpected();
    }

    @Data
    public static class ProcessorItem {

        private final PricingResults results;
        private Set<PortfolioId> portfolioIds;
        private List<VariationMargin> expected;
        private List<VariationMargin> simulated;

        public ProcessorItem(PricingResults results) {
            this.results = results;
        }
    }

    public interface PricingResultProcessor {
        ProcessorItem process(ProcessorItem processorItem);
        void setNextProcessor(PricingResultProcessor nextProcessor);
    }
}
