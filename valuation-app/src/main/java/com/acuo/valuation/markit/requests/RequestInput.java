package com.acuo.valuation.markit.requests;

import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.valuation.requests.Request;
import com.acuo.valuation.requests.RequestData;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@XmlRootElement(name = "presentvalue")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestInput {

    public RequestInput() {
    }

    public RequestInput(LocalDate valuationDate, String valuationCurrency, RequestData data) {
        this.valuationDate = valuationDate;
        this.valuationCurrency = valuationCurrency;
        this.data = new RequestDataInput(data);
    }

    private RequestInput(Request request) {
        this(request.getValuationDate(), request.getValuationCurrency(), request.getData());
    }

    public static RequestInput definition(Request request) {
        return new RequestInput(request);
    }

    public Request request() {
        return MarkitRequest.of(this);
    }

    @XmlPath("valuationdate/text()")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    LocalDate valuationDate;

    @XmlPath("valuationcurrency/text()")
    String valuationCurrency;

    @XmlElement(name = "data", required = true)
    public RequestDataInput data;
}
