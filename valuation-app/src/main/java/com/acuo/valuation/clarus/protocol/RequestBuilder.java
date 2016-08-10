package com.acuo.valuation.clarus.protocol;

import com.acuo.valuation.clarus.protocol.Clarus.CalculationMethod;
import com.acuo.valuation.clarus.protocol.Clarus.MarginMethodology;
import com.acuo.valuation.clarus.protocol.Clarus.ResultStats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    private final LocalDate valueDate;
    private final MarginMethodology ccp;
    private String currency = "USD";
    private List<PortfolioData> portfolios = new ArrayList<>();
    private List<PortfolioData> whatIfs = new ArrayList<>();
    private boolean failOnWarning = false;
    private CalculationMethod calculationMethod = CalculationMethod.Optimised​;
    private ResultStats resultStats = ResultStats.Default;

    private RequestBuilder(LocalDate valueDate, MarginMethodology methodology) {
        this.valueDate = valueDate;
        this.ccp = methodology;
    }

    public static RequestBuilder create(LocalDate valueDate, MarginMethodology methodology) {
        return new RequestBuilder(valueDate, methodology);
    }

    public RequestBuilder reportingCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public RequestBuilder failOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    public RequestBuilder calculationMethod(CalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
        return this;
    }

    public RequestBuilder resultStats(ResultStats resultStats) {
        this.resultStats = resultStats;
        return this;
    }

    public RequestBuilder portfolioData(PortfolioData portfolioData) {
        this.portfolios.add(portfolioData);
        return this;
    }

    public RequestBuilder whatIfsData(PortfolioData whatIfsData) {
        this.whatIfs.add(whatIfsData);
        return this;
    }

    public Request build() {
        return new Request(this.valueDate, this.ccp, this.portfolios, this.whatIfs, this.failOnWarning, this.calculationMethod, this.resultStats);
    }
}
