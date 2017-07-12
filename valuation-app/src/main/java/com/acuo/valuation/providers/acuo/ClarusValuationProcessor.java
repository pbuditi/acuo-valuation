package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.MarginCallService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.calls.CallGeneratorProcessor;
import com.acuo.valuation.providers.acuo.calls.CallProcessorItem;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class ClarusValuationProcessor {

    private final ResultPersister<MarginResults> resultPersister;
    private final CallGeneratorProcessor callGeneratorProcessor;
    private final MarginCallService marginCallService;

    @Inject
    public ClarusValuationProcessor(ResultPersister<MarginResults> resultPersister,
                                    @Named("clarus") CallGeneratorProcessor callGeneratorProcessor,
                                    MarginCallService marginCallService) {
        this.resultPersister = resultPersister;
        this.callGeneratorProcessor = callGeneratorProcessor;
        this.marginCallService = marginCallService;
    }

    public List<MarginCall> process(MarginResults results) {
        log.info("starting pricing result processing");
        Set<PortfolioId> portfolioIds = resultPersister.persist(results);
        CallProcessorItem item = new CallProcessorItem(results.getValuationDate(), results.getMarginType(), portfolioIds);
        CallProcessorItem callProcessorItem = callGeneratorProcessor.process(item);
        log.info("pricing result processing ended");
        final List<String> ids = callProcessorItem.getExpected();
        return ids.stream().map(id -> marginCallService.find(id, 3)).collect(toList());
    }
}
