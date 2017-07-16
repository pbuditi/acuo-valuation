package com.acuo.valuation.services;

import com.acuo.persist.entity.Portfolio;

import java.io.InputStream;
import java.util.List;

public interface TradeUploadService {

    List<String> fromExcel(InputStream fis);

    List<Portfolio> fromExcelWithValues(InputStream fis);

}
