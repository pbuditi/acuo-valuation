package com.acuo.valuation.services;

import com.acuo.persist.entity.MarginCall;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MarginCallGenService {

    List<MarginCall> marginCalls(Set<String> portfolioSet, LocalDate date);

}
