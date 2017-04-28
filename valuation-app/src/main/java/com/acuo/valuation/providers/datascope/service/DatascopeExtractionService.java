package com.acuo.valuation.providers.datascope.service;

import java.util.List;

public interface DatascopeExtractionService {

    List<String> getExtractionFileId(String token, String scheduleId);
}
