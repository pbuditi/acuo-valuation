package com.acuo.valuation.providers.clarus.protocol;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.*;

@Value
public class Envelope {
    LocalDate valueDate;
    MarginMethodology ccp;
    HouseClient houseClient = HouseClient.Client;
    boolean calcAddons = false;
    String reportingCcy = "USD";
    List<PortfolioData> portfolioData;
    List<PortfolioData> whatIfData;
    CalculationMethod varCalcMethod;
    ResultStats resultStats;
}
