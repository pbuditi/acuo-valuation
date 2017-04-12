package com.acuo.valuation.providers.holiday.services;

import com.acuo.common.http.client.ClientEndPoint;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class HolidayServiceImpl implements HolidayService {

    private final ClientEndPoint<HolidayEndPointConfig> client;

    @Inject
    public HolidayServiceImpl(ClientEndPoint<HolidayEndPointConfig> client)
    {
        this.client = client;
    }

    public boolean queryHoliday(LocalDate date)
    {
        String response =  HolidayCall.of(client).with("date", date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).create().send();
        return Boolean.valueOf(response.toLowerCase());
    }
}
