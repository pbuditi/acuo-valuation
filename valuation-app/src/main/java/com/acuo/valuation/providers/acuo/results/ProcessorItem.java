package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.MarkitResults;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ProcessorItem<R> {

    private final R results;
    private Set<PortfolioId> portfolioIds;
    private List<VariationMargin> expected;
    private List<VariationMargin> simulated;

    public ProcessorItem(R results) {
        this.results = results;
    }
}
