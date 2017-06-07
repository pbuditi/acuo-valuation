package com.acuo.valuation.protocol.results;

import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkitValuationTest {

    @Test
    public void simpleChecks() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setTradeId("id1");
        markitValue.setPv(1.0d);
        MarkitValuation markitValuation = new MarkitValuation(markitValue);

        assertThat(markitValuation.getTradeId()).isEqualTo("id1");
        assertThat(markitValuation.getValue().getValue()).isEqualTo(1.0d);
    }

    @Test
    public void testEquals() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setTradeId("id1");
        markitValue.setPv(1.0d);
        MarkitValuation m1 = new MarkitValuation(markitValue);
        MarkitValuation m2 = new MarkitValuation(markitValue);

        assertThat(m1).isEqualTo(m2);
    }

    @Test
    public void testPvSum() {
        double[] pvs = {1.0d, 2.0d, 3.0d};

        List<MarkitValue> values = new ArrayList<>();
        for (int i = 0; i < pvs.length; i++) {
            MarkitValue value = new MarkitValue();
            value.setTradeId("id");
            value.setPv(pvs[i]);
            values.add(value);
        }
        MarkitValuation valuation = new MarkitValuation(values.toArray(new MarkitValue[values.size()]));

        assertThat(valuation.getValue().getValue()).isEqualTo(6.0d);
    }
}