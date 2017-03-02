package com.acuo.valuation.services;

import java.io.InputStream;
import java.util.List;

public interface TradeUploadService {

    List<String> uploadTradesFromExcel(InputStream fis);
}
