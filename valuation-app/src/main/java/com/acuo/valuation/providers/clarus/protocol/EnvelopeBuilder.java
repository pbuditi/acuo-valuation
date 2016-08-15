package com.acuo.valuation.providers.clarus.protocol;

import com.acuo.valuation.providers.clarus.protocol.Clarus.CalculationMethod;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginMethodology;
import com.acuo.valuation.providers.clarus.protocol.Clarus.ResultStats;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnvelopeBuilder {

    private final ObjectMapper objectMapper;
    private LocalDate valueDate = LocalDate.now();
    private MarginMethodology marginMethodology;
    private String currency = "USD";
    private List<PortfolioData> portfolios = null;
    private List<PortfolioData> whatIfs = null;
    private boolean failOnWarning = false;
    private CalculationMethod calculationMethod = CalculationMethod.Optimised​;
    private ResultStats resultStats = ResultStats.Default;

    private EnvelopeBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    static EnvelopeBuilder create(ObjectMapper objectMapper) {
        return new EnvelopeBuilder(objectMapper);
    }

    EnvelopeBuilder marginMethodology(MarginMethodology marginMethodology) {
        this.marginMethodology = marginMethodology;
        return this;
    }

    EnvelopeBuilder valueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    EnvelopeBuilder reportingCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    EnvelopeBuilder failOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    EnvelopeBuilder calculationMethod(CalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
        return this;
    }

    EnvelopeBuilder resultStats(ResultStats resultStats) {
        this.resultStats = resultStats;
        return this;
    }

    EnvelopeBuilder portfolioData(PortfolioData portfolioData) {
        if (this.portfolios == null) this.portfolios = new ArrayList<>();
        this.portfolios.add(portfolioData);
        return this;
    }

    EnvelopeBuilder whatIfsData(PortfolioData whatIfsData) {
        if (this.whatIfs == null) this.whatIfs = new ArrayList<>();
        this.whatIfs.add(whatIfsData);
        return this;
    }

    Envelope build() {
        return new Envelope(this.valueDate, this.marginMethodology, this.portfolios, this.whatIfs, this.failOnWarning, this.calculationMethod, this.resultStats);
    }

    String asJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(build());
    }
}
