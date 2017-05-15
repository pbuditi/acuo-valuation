package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.PortfolioId;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ProcessorItem<R> {

    private final R results;
    private Set<PortfolioId> portfolioIds;
    private List<MarginCall> expected;
    private List<MarginCall> simulated;

    public ProcessorItem(R results) {
        this.results = results;
    }
}
