package com.acuo.valuation.services;

import com.acuo.valuation.protocol.results.SwapResults;

public interface SwapService {

    SwapResults getPv(String swapId);

}
