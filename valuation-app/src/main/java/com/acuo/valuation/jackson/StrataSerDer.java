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
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.schedule.Frequency;
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
        strataModule.addSerializer(new BusinessDayConventionSerializer(BusinessDayConvention.class));
        strataModule.addDeserializer(BusinessDayConvention.class, new BusinessDayConventionDeserializer(BusinessDayConvention.class));
        strataModule.addSerializer(new FrequencySerializer(Frequency.class));
        strataModule.addDeserializer(Frequency.class, new FrequencyDeserializer(Frequency.class));
        strataModule.addSerializer(new FloatingRateNameSerializer(FloatingRateName.class));
        strataModule.addDeserializer(FloatingRateName.class, new FloatingRateNameDeserializer(FloatingRateName.class));
        strataModule.addSerializer(new TenorSerializer(Tenor.class));
        strataModule.addDeserializer(Tenor.class, new TenorDeserializer(Tenor.class));
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
            jgen.writeString(currency.getCode());
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
        public DayCount deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return DayCount.of(p.getText());
        }
    }

    private static class BusinessDayConventionSerializer extends StdSerializer<BusinessDayConvention> {

        private BusinessDayConventionSerializer(Class<BusinessDayConvention> t) {
            super(t);
        }

        @Override
        public void serialize(BusinessDayConvention value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getName());
        }
    }

    private static class BusinessDayConventionDeserializer extends StdDeserializer<BusinessDayConvention> {

        private BusinessDayConventionDeserializer(Class<BusinessDayConvention> t) {
            super(t);
        }

        @Override
        public BusinessDayConvention deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return BusinessDayConvention.of(p.getText());
        }
    }

    private static class FrequencySerializer extends StdSerializer<Frequency> {

        private FrequencySerializer(Class<Frequency> t) {
            super(t);
        }

        @Override
        public void serialize(Frequency value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }

    private static class FrequencyDeserializer extends StdDeserializer<Frequency> {

        private FrequencyDeserializer(Class<Frequency> t) {
            super(t);
        }

        @Override
        public Frequency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Frequency.parse(p.getText());
        }
    }

    private static class FloatingRateNameSerializer extends StdSerializer<FloatingRateName> {

        private FloatingRateNameSerializer(Class<FloatingRateName> t) {
            super(t);
        }

        @Override
        public void serialize(FloatingRateName value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }

    private static class FloatingRateNameDeserializer extends StdDeserializer<FloatingRateName> {

        private FloatingRateNameDeserializer(Class<FloatingRateName> t) {
            super(t);
        }

        @Override
        public FloatingRateName deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return FloatingRateName.of(p.getText());
        }
    }

    private static class TenorSerializer extends StdSerializer<Tenor> {

        private TenorSerializer(Class<Tenor> t) {
            super(t);
        }

        @Override
        public void serialize(Tenor value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }

    private static class TenorDeserializer extends StdDeserializer<Tenor> {

        private TenorDeserializer(Class<Tenor> t) {
            super(t);
        }

        @Override
        public Tenor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Tenor.parse(p.getText());
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
