package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.margin.Types;
import com.acuo.common.model.ids.PortfolioId;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class CallProcessorItem {

    private final LocalDate valuationDate;
    private final Types.CallType callType;
    private final Set<PortfolioId> portfolioIds;
    private List<String> expected;
    private List<String> simulated;
}
