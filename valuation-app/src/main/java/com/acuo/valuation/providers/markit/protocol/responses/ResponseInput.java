package com.acuo.valuation.providers.markit.protocol.responses;

import com.acuo.common.marshal.DecimalAdapter;
import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.valuation.providers.markit.protocol.responses.MarkitResponse.MarkitResponseBuilder;
import com.acuo.valuation.protocol.responses.Header;
import com.acuo.valuation.protocol.responses.Response;
import com.acuo.valuation.protocol.results.Value;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseInput {

    public ResponseInput() {
        header = new ResponseHeader();
        values = new ArrayList<>();
    }

    static class ResponseHeader {

        @XmlPath("name/text()")
        String name;

        @XmlPath("version/text()")
        String version;

        @XmlPath("date/text()")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        LocalDate date;

        @XmlPath("valuationdate/text()")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        LocalDate valuationDate;

        @XmlPath("valuationccy/text()")
        String valuationCurrency;

        @XmlPath("numtradessuccessful/text()")
        Integer successfulTrades;

        @XmlPath("numtradesfailed/text()")
        Integer failedTrades;

        @XmlPath("totalnumtrades/text()")
        Integer totalTrades;

        @XmlPath("valuationscomplete/text()")
        Boolean valuationsComplete;
    }

    @XmlElement(name = "header", required = true)
    public final ResponseHeader header;

    static class ResponseValue {

        @XmlPath("TradeId/text()")
        String tradeId;

        @XmlPath("Book/text()")
        String book;

        @XmlPath("PVLocal/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double pvLocal;

        @XmlPath("PortfolioValuationsLocal/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double portfolioValuationsLocal;

        @XmlPath("PresentValue/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double pv;

        @XmlPath("Accrued/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double accrued;

        @XmlPath("ParRate/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double parRate;

        @XmlPath("LocalCcy/text()")
        String localCurrency;

        @XmlPath("Status/text()")
        String status;

        @XmlPath("LegId/text()")
        String legId;

        @XmlPath("Notional/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double notional;

        @XmlPath("ValuationCcy/text()")
        String valuationCcy;

        @XmlPath("PV01/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double pv01;

        @XmlPath("CleanPVLocal/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double cleanPVLocal;

        @XmlPath("CleanPV/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double cleanPV;

        @XmlPath("AccruedValCcy/text()")
        String accruedValCcy;

        @XmlPath("DirtyPrice/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double dirtyPrice;

        @XmlPath("CleanPrice/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double cleanPrice;

        @XmlPath("PriceAccrued/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double priceAccrued;

        @XmlPath("PV01Local/text()")
        @XmlJavaTypeAdapter(DecimalAdapter.class)
        Double pv01Local;

        @XmlPath("FAS157Rating/text()")
        String fas157Rating;

        @XmlPath("InstrumentCode/text()")
        String instrumentCode;

        @XmlPath("InstrumentType/text()")
        String instrumentType;

        @XmlPath("ErrorMessage/text()")
        String errorMessage;
    }

    @XmlElement(name = "value")
    public final List<ResponseValue> values;

    public static ResponseInput definition(Response response) {
        return new ResponseDefinitionBuilder(response).build();
    }

    public static class ResponseDefinitionBuilder {

        private final Response response;

        public ResponseDefinitionBuilder(Response response) {
            this.response = response;
        }

        public ResponseInput build() {
            ResponseInput definition = new ResponseInput();
            populateHeader(definition);
            populateValues(definition);
            return definition;
        }

        private void populateHeader(ResponseInput definition) {
            ResponseHeader header = definition.header;
            Header h = response.header();
            header.date = h.getDate();
            header.failedTrades = h.getFailedTrades();
            header.name = h.getName();
            header.successfulTrades = h.getSuccessfulTrades();
            header.totalTrades = h.getTotalTrades();
            header.valuationCurrency = h.getValuationCurrency();
            header.valuationDate = h.getValuationDate();
            header.valuationsComplete = h.getValuationsComplete();
            header.version = h.getVersion();
        }

        private void populateValues(ResponseInput definition) {
            response.values().stream().forEach(v -> populateValue(v, definition));
        }

        private void populateValue(Value v, ResponseInput definition) {
            ResponseValue value = new ResponseValue();
            value.accrued = v.getAccrued();
            value.book = v.getBook();
            value.legId = v.getLegId();
            value.localCurrency = v.getLocalCurrency();
            value.notional = v.getNotional();
            value.parRate = v.getParRate();
            value.pv = v.getPv();
            value.pvLocal = v.getPvLocal();
            value.portfolioValuationsLocal = v.getPortfolioValuationsLocal();
            value.status = v.getStatus();
            value.tradeId = v.getTradeId();

            value.valuationCcy = v.getValuationCcy();
            value.pv01 = v.getPv01();
            value.cleanPVLocal = v.getCleanPVLocal();
            value.cleanPV = v.getCleanPV();
            value.accruedValCcy = v.getAccruedValCcy();
            value.dirtyPrice = v.getDirtyPrice();
            value.cleanPrice = v.getCleanPrice();
            value.priceAccrued = v.getPriceAccrued();
            value.pv01Local = v.getPv01Local();
            value.fas157Rating = v.getFas157Rating();
            value.instrumentCode = v.getInstrumentCode();
            value.instrumentType = v.getInstrumentType();
            value.errorMessage = v.getErrorMessage();

            definition.values.add(value);
        }
    }

    public Response response() {
        MarkitResponseBuilder builder = new MarkitResponseBuilder(header());
        addValues(builder);
        return builder.build();
    }

    private MarkitHeader header() {
        MarkitHeader markitHeader = new MarkitHeader();
        markitHeader.setName(header.name);
        markitHeader.setVersion(header.version);
        markitHeader.setDate(header.date);
        markitHeader.setValuationDate(header.valuationDate);
        markitHeader.setValuationCurrency(header.valuationCurrency);
        markitHeader.setSuccessfulTrades(header.successfulTrades);
        markitHeader.setFailedTrades(header.failedTrades);
        markitHeader.setTotalTrades(header.totalTrades);
        markitHeader.setValuationsComplete(header.valuationsComplete);
        return markitHeader;
    }

    private void addValues(MarkitResponseBuilder builder) {
        values.stream().map(valueDef -> value(valueDef)).forEach(value -> builder.addValue(value));
    }

    private MarkitValue value(ResponseValue v) {
        MarkitValue value = new MarkitValue();
        value.setTradeId(v.tradeId);
        value.setBook(v.book);
        value.setPvLocal(v.pvLocal);
        value.setPortfolioValuationsLocal(v.portfolioValuationsLocal);
        value.setPv(v.pv);
        value.setAccrued(v.accrued);
        value.setParRate(v.parRate);
        value.setLocalCurrency(v.localCurrency);
        value.setStatus(v.status);
        value.setLegId(v.legId);
        value.setNotional(v.notional);

        value.setValuationCcy(v.valuationCcy);
        value.setPv01(v.pv01);
        value.setCleanPVLocal(v.cleanPVLocal);
        value.setCleanPV(v.cleanPV);
        value.setAccruedValCcy(v.accruedValCcy);
        value.setDirtyPrice(v.dirtyPrice);
        value.setCleanPrice(v.cleanPrice);
        value.setPriceAccrued(v.priceAccrued);
        value.setPv01Local(v.pv01Local);
        value.setFas157Rating(v.fas157Rating);
        value.setInstrumentCode(v.instrumentCode);
        value.setInstrumentType(v.instrumentType);
        value.setErrorMessage(v.errorMessage);

        return value;
    }
}
