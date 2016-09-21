package com.acuo.valuation.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StrataSerDer {

    private final SimpleModule strataModule;

    public StrataSerDer() {
        strataModule = new SimpleModule();
        strataModule.addSerializer(new CurrencySerializer(Currency.class));
        strataModule.addDeserializer(Currency.class, new CurrencyDeserializer(Currency.class));
        strataModule.addSerializer(new DayCountProxySerializer(DayCount.class));
        strataModule.addDeserializer(DayCount.class, new DayCountProxyDeserializer(DayCount.class));
        strataModule.addSerializer(new ResultSerializer(Result.class));
    }

    public SimpleModule strataModule() {
        return strataModule;
    }

    private static class CurrencySerializer extends StdSerializer<Currency> {

        private CurrencySerializer(Class<Currency> t) {
            super(t);
        }

        @Override
        public void serialize(Currency currency,
                              JsonGenerator jgen,
                              SerializerProvider sp) throws IOException {
            jgen.writeStartObject();
            jgen.writeString(currency.getCode());
            jgen.writeEndObject();
        }
    }

    private static class CurrencyDeserializer extends StdDeserializer<Currency> {

        private CurrencyDeserializer(Class<Currency> t) {
            super(t);
        }

        @Override
        public Currency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Currency.parse(p.getText());
        }
    }

    private static class DayCountProxySerializer extends StdSerializer<DayCount> {

        private DayCountProxySerializer(Class<DayCount> t) {
            super(t);
        }

        @Override
        public void serialize(DayCount value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getName());
        }
    }

    private static class DayCountProxyDeserializer extends StdDeserializer<DayCount> {

        private DayCountProxyDeserializer(Class<DayCount> t) {
            super(t);
        }

        @Override
        public DayCount deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }

    private static class ResultSerializer extends StdSerializer<Result> {

        private ResultSerializer(Class<Result> t) {
            super(t);
        }

        @Override
        public void serialize(Result value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            value.stream()
                    .forEach(v -> writeObject(v, gen));
            //gen.writeString(value.toString());
        }

        private void writeObject(Object value, JsonGenerator gen) {
            try {
                gen.writeObject(value);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
