package com.acuo.valuation.services;

import java.io.InputStream;
import java.util.List;

public interface TradeUploadService {

    List<String> fromExcel(InputStream fis);

    List<String> fromExcelNew(InputStream fis);
}
