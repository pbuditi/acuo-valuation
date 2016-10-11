package com.acuo.valuation.protocol.results;

import com.acuo.valuation.providers.markit.protocol.responses.MarkitValue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.meanbean.test.BeanTester;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkitValuationTest {

    @Test
    public void simpleChecks() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        MarkitValuation markitValuation = new MarkitValuation(markitValue);

        assertThat(markitValuation.getValues()).hasSize(1);
        assertThat(markitValuation.getPv()).isEqualTo(1.0d);
    }

    @Test
    public void testEquals() {
        MarkitValue markitValue = new MarkitValue();
        markitValue.setPv(1.0d);
        MarkitValuation m1 = new MarkitValuation(markitValue);
        MarkitValuation m2 = new MarkitValuation(markitValue);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.canEqual(m2)).isTrue();
    }

    @Test
    public void testPvSum() {
        double[] pvs = {1.0d, 2.0d, 3.0d};

        List<MarkitValue> values = new ArrayList<>();
        for (int i = 0; i < pvs.length; i++) {
            MarkitValue value = new MarkitValue();
            value.setPv(pvs[i]);
            values.add(value);
        }
        MarkitValuation valuation = new MarkitValuation(values.toArray(new MarkitValue[values.size()]));

        assertThat(valuation.getPv()).isEqualTo(6.0d);
    }

    @Test
    public void equalsAndHashCodeContract() throws Exception {
        EqualsVerifier.forClass(MarkitValuation.class)
                      .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
                      .verify();
    }
}