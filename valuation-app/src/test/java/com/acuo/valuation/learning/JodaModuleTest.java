package com.acuo.valuation.learning;

import com.acuo.valuation.jackson.StrataSerDer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opengamma.strata.basics.currency.Currency;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JodaModuleTest {

    static class Bean {
        public DateTime start;
        public Currency currency;
    }

    ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new StrataSerDer().strataModule());
    }

    @Test
    public void testJodaBean() throws IOException {
        final String INPUT_JSON = "{\"start\" : \"1972-12-28T12:00:01.000Z\", \"currency\" : \"USD\"}";
        Bean bean = mapper.readValue(INPUT_JSON, Bean.class);
        assertThat(bean).isNotNull();
    }

}
