package com.acuo.valuation.providers.clarus.services;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;

import java.util.List;

public interface ClarusMarginService {

    MarginResults send(List<com.acuo.common.model.trade.Trade> trades, DataModel model, MarginCallType callType);


}
