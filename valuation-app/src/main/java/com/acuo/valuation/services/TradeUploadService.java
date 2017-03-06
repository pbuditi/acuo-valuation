package com.acuo.valuation.services;

import com.acuo.valuation.jackson.MarginCallDetail;

import java.io.InputStream;

public interface TradeUploadService {

    MarginCallDetail uploadTradesFromExcel(InputStream fis);
}
