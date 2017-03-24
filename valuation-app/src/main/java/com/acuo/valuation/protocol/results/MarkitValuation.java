package com.acuo.valuation.protocol.results;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class MarkitValuation {

    private final List<Value> values;

    public MarkitValuation(List<Value> values) {
        this.values = values;
    }

    public MarkitValuation(Value... values) {
        this.values = Arrays.asList(values);
    }

    public Double getPv() {
        return values.stream().map(Value::getPv).reduce(0.0d, (a, b) -> a + b);
    }

}
