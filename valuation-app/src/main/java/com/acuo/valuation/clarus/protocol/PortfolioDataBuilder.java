package com.acuo.valuation.clarus.protocol;

import com.acuo.valuation.clarus.protocol.Clarus.DataFormat;
import com.acuo.valuation.clarus.protocol.Clarus.DataType;

public class PortfolioDataBuilder {

    DataFormat format;
    DataType type;
    String data;

    private PortfolioDataBuilder() {
    }

    public static PortfolioDataBuilder create() {
        return new PortfolioDataBuilder();
    }

    public PortfolioDataBuilder addData(String data) {
        this.data = data;
        return this;
    }

    public PortfolioDataBuilder addType(DataType type) {
        this.type = type;
        return this;
    }

    public PortfolioDataBuilder addFormat(DataFormat format) {
        this.format = format;
        return this;
    }

    public PortfolioData build() {
        return new PortfolioData(this.format, this.type, this.data);
        //return pairs.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
