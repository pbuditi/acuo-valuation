package com.acuo.valuation.services;

import java.io.InputStream;

public interface TradeUploadService {

    boolean uploadTradesFromExcel(InputStream fis);
}
