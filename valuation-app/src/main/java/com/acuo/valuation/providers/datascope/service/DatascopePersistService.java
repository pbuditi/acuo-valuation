package com.acuo.valuation.providers.datascope.service;

import java.util.List;

public interface DatascopePersistService {

    void persistFxRate(List<String> csvLine);
}
