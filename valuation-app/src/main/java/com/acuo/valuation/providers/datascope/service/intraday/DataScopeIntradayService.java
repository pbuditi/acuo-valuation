package com.acuo.valuation.providers.datascope.service.intraday;

import com.opengamma.strata.basics.currency.FxRate;

import java.util.List;

public interface DataScopeIntradayService {

    List<FxRate> rates();
}
