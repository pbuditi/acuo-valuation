package com.acuo.valuation.providers.clarus.protocol;

import com.acuo.valuation.providers.clarus.protocol.Clarus.DataFormat;
import com.acuo.valuation.providers.clarus.protocol.Clarus.DataType;

public class PortfolioDataBuilder {

    private DataFormat format;
    private DataType type;
    private String data;

    private PortfolioDataBuilder() {
    }

    static PortfolioDataBuilder create() {
        return new PortfolioDataBuilder();
    }

    PortfolioDataBuilder addData(String data) {
        this.data = data;
        return this;
    }

    PortfolioDataBuilder addType(DataType type) {
        this.type = type;
        return this;
    }

    PortfolioDataBuilder addFormat(DataFormat format) {
        this.format = format;
        return this;
    }

    PortfolioData build() {
        return new PortfolioData(this.format, this.type, this.data);
    }
}
