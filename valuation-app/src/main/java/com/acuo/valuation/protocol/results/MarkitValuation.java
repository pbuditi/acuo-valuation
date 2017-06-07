package com.acuo.valuation.protocol.results;

import com.opengamma.strata.collect.result.FailureItem;
import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.ValueWithFailures;
import lombok.Data;
import org.neo4j.helpers.collection.Iterators;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Data
public class MarkitValuation {

    private final ValueWithFailures<Double> value;
    private final String tradeId;

    public MarkitValuation(List<Value> values) {
        this.tradeId = checkUniqueTradeId(values);
        Double pv = computePv(values);
        List<FailureItem> failures = failures(values);
        this.value = ValueWithFailures.of(pv, failures);
    }

    public MarkitValuation(Value... values) {
        this(Arrays.asList(values));
    }

    private Double computePv(List<Value> values) {
        return values.stream()
                .filter(value -> !"Failed".equalsIgnoreCase(value.getStatus()))
                .map(Value::getPv)
                .reduce(0.0d, (a, b) -> a + b);
    }

    private List<FailureItem> failures(List<Value> values) {
        return values.stream()
                .filter(value -> "Failed".equalsIgnoreCase(value.getStatus()))
                .map(value -> FailureItem.of(FailureReason.ERROR, "{}", value.getErrorMessage()))
                .collect(toList());
    }

    private String checkUniqueTradeId(List<Value> values) {
        Set<String> tradeIds = values.stream().map(Value::getTradeId).collect(toSet());
        if(tradeIds.size() == 1) {
            return Iterators.single(tradeIds.iterator());
        } else {
            throw new RuntimeException("Cannot set markit valuation with multiple trade ids");
        }
    }

}