package com.acuo.valuation.providers.datascope.service;

public interface DatascopeScheduleService {

    String scheduleFXRateExtraction(String token);

    String scheduleBondExtraction(String token);
}
