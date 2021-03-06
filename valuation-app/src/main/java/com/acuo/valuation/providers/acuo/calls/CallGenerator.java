package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.ids.PortfolioId;
import com.acuo.common.model.margin.Types;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.InitialMargin;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.services.CallService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Slf4j
public class CallGenerator extends AbstractCallGeneratorProcessor implements CallService {

    private final ValuationService valuationService;
    private final MarginStatementService marginStatementService;
    private final MarginCallService marginCallService;
    private final AgreementService agreementService;
    private final CurrencyService currencyService;
    private final PortfolioService portfolioService;

    @Inject
    CallGenerator(ValuationService valuationService,
                  MarginStatementService marginStatementService,
                  MarginCallService marginCallService,
                  CurrencyService currencyService,
                  AgreementService agreementService,
                  PortfolioService portfolioService) {
        this.valuationService = valuationService;
        this.marginStatementService = marginStatementService;
        this.marginCallService = marginCallService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
        this.portfolioService = portfolioService;
    }

    @Override
    public CallProcessorItem process(CallProcessorItem item) {
        log.info("processing {}", item);
        LocalDate valuationDate = item.getValuationDate();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Types.CallType callType = item.getCallType();
        Set<PortfolioId> portfolioIds = item.getPortfolioIds();
        List<String> marginCalls = createCalls(portfolioIds, valuationDate, callDate, callType);
        log.info("generated {} expected calls", marginCalls.size());
        item.setExpected(marginCalls);
        if (next != null)
            return next.process(item);
        else
            return item;
    }

    public List<String> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate, Types.CallType callType) {
        log.info("generating margin calls: portfolios {}, valuation date [{}], call date [{}] and call type [{}]",
                portfolioSet, valuationDate, callDate, callType);
        return portfolioSet.stream()
                .map(id -> valuationService.getMarginValuationFor(id, callType))
                .filter(Objects::nonNull)
                .map(valuation -> createcalls(sideSupplier().get(), valuation, valuationDate, callDate, statementStatusSupplier().get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MarginCall::getItemId)
                .collect(toList());
    }

    private Optional<MarginCall> createcalls(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus) {
        final Agreement agreement = agreementService.agreementFor(valuation.getPortfolio().getPortfolioId());
        final Map<Currency, Double> rates = currencyService.getAllFX();
        return convert(side, valuation, valuationDate, callDate, statementStatus, agreement, rates);
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected Supplier<Side> sideSupplier() {
        return () -> Side.Client;
    }

    private Optional<MarginCall> convert(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<MarginValue>> currents = marginValueRelation(valuation, valuationDate);
        Types.CallType callType = valuation.getCallType();
        Optional<Double> amount = currents.map(this::sum);
        Long tradeCount = portfolioService.tradeCount(valuation.getPortfolio().getPortfolioId());
        return amount.map(aDouble -> process(callType, side, aDouble, Currency.USD, statementStatus, agreement, valuationDate, callDate, rates, tradeCount));
    }

    private Optional<List<MarginValue>> marginValueRelation(MarginValuation valuation, LocalDate valuationDate) {
        Set<MarginValue> values = valuation.getValues();
        if (values != null) {
            List<MarginValue> result = valuation.getValues()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(value -> value.getValuationDate().equals(valuationDate))
                    .collect(toList());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Double sum(List<MarginValue> values) {
        return values.stream()
                .mapToDouble(MarginValue::getAmount)
                .sum();
    }

    protected MarginCall process(Types.CallType callType,
                                 Side side,
                                 Double amount,
                                 Currency currency,
                                 StatementStatus statementStatus,
                                 Agreement agreement,
                                 LocalDate valuationDate,
                                 LocalDate callDate,
                                 Map<Currency, Double> rates,
                                 Long tradeCount) {
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, callDate);
        MarginCall marginCall;
        if (callType.equals(Types.CallType.Variation)) {
            marginCall = new VariationMargin(side, amount, valuationDate, callDate, currency, agreement, marginStatement, rates, tradeCount);
        } else {
            marginCall = new InitialMargin(side, amount, valuationDate, callDate, currency, agreement, marginStatement, rates, tradeCount);
        }

        final MarginCall toDelete = marginCallService.find(marginCall.getItemId());
        if (toDelete != null) {
            marginCallService.delete(marginCallService.find(marginCall.getItemId()));
        }

        marginCall.setMarginStatement(marginStatement);
        marginCall = marginCallService.save(marginCall);
        marginCall = marginStatementService.setStatus(marginCall.getItemId(), statementStatus);
        return marginCall;

    }
}