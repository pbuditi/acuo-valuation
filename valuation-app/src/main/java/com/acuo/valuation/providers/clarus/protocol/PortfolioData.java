package com.acuo.valuation.providers.clarus.protocol;

import lombok.Value;

@Value
public class PortfolioData {
    Clarus.DataFormat dataFormat;
    Clarus.DataType dataType;
    String data;
}
