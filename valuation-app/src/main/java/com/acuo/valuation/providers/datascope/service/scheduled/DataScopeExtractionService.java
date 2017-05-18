package com.acuo.valuation.providers.datascope.service.scheduled;

import java.util.List;

public interface DataScopeExtractionService {

    List<String> getExtractionFileId(String token, String scheduleId);
}
