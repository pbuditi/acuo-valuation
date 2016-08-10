package com.acuo.valuation.clarus.protocol;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;

import static com.acuo.valuation.clarus.protocol.Clarus.*;

@Value
public class Request {
    LocalDate valueDate;
    MarginMethodology ccp;
    String reportingCcy = "USD";
    List<PortfolioData> portfolioData;
    List<PortfolioData> whatIfData;
    boolean failOnWarning;
    CalculationMethod varCalcMethod;
    ResultStats resultStats;
}
