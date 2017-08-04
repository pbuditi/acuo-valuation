package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.ids.PortfolioId;
import com.acuo.common.model.margin.Types;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.services.MarginCallService;
import com.acuo.valuation.providers.acuo.calls.CallGeneratorProcessor;
import com.acuo.valuation.providers.acuo.calls.CallProcessorItem;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.acuo.common.model.margin.Types.CallType.Initial;
import static com.acuo.common.model.margin.Types.CallType.Variation;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Singleton
public class PortfolioProcessor {

    private final CallGeneratorProcessor callGeneratorProcessor;
    private final MarginCallService marginCallService;

    private final Set<Types.CallType> callTypes = ImmutableSet.of(Variation, Initial);

    @Inject
    public PortfolioProcessor(CallGeneratorProcessor callGeneratorProcessor, MarginCallService marginCallService) {
        this.callGeneratorProcessor = callGeneratorProcessor;
        this.marginCallService = marginCallService;
    }

    public List<MarginCall> process(Set<PortfolioId> portfolioIds) {
        log.info("starting portfolio valuation processing");
        LocalDate valuationDate = LocalDateUtils.valuationDate();
        Set<String> ids = callTypes.stream()
                .map(callType -> {
                    CallProcessorItem item = new CallProcessorItem(valuationDate, callType, portfolioIds);
                    CallProcessorItem callProcessorItem = callGeneratorProcessor.process(item);
                    return callProcessorItem.getExpected();
                })
                .flatMap(Collection::stream)
                .sorted()
                .collect(toSet());
        log.info("portfolio valuation processing ended");
        Iterable<MarginCall> calls = marginCallService.calls(ids.toArray(new String[ids.size()]));
        return StreamSupport.stream(calls.spliterator(), true).collect(toList());
    }
}
