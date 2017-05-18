package com.acuo.valuation.providers.datascope.service.scheduled;

public interface DataScopeScheduleService {

    String scheduleFXRateExtraction(String token);

    String scheduleBondExtraction(String token);
}
