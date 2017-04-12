package com.acuo.valuation.providers.holiday.services;

import java.time.LocalDate;

public interface HolidayService {

    boolean queryHoliday(LocalDate date);
}
