package com.acuo.valuation.protocol.results;

import lombok.Data;
import org.neo4j.helpers.collection.Iterators;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Data
public class MarkitValuation {

    private final Double pv;
    private final String tradeId;

    public MarkitValuation(List<Value> values) {
        this.pv = computePv(values);
        this.tradeId = checkUniqueTradeId(values);
    }

    private Double computePv(List<Value> values) {
        return values.stream().map(Value::getPv).reduce(0.0d, (a, b) -> a + b);
    }

    public MarkitValuation(Value... values) {
        List<Value> v = Arrays.asList(values);
        this.pv = computePv(v);
        this.tradeId = checkUniqueTradeId(v);
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