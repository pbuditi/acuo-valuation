package com.acuo.valuation.providers.clarus.protocol;

import lombok.Value;

import java.time.LocalDate;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;

@Value
public class Request {
    LocalDate valueDate;
    DataModel model;
    String portfolios;
    String whatif;
}
