package com.acuo.valuation.providers.datascope.service;

import java.util.List;

public interface DataScopePersistService {

    void persistFxRate(List<String> csvLine);

    void persistBond(List<String> csvLine);
}
