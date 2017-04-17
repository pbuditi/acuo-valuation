package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.schedule.Recurrence;
import com.acuo.valuation.providers.datascope.protocol.schedule.ScheduleRequestJson;
import com.acuo.valuation.providers.datascope.protocol.schedule.ScheduleResponseJson;
import com.acuo.valuation.providers.datascope.protocol.schedule.Trigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DatascopeScheduleServiceImpl implements DatascopeScheduleService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    @Inject
    public DatascopeScheduleServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        objectMapper = new ObjectMapper();
    }


    public String sheduleExtraction(String token)
    {
        String scheduleId = null;
        String response = DatascopeScheduleCall.of(client).with("token",token).with("body", buildScheduleRequestJson()).create().send();
        log.info(response);

        try
        {
            scheduleId = objectMapper.readValue(response, ScheduleResponseJson.class).getScheduleId();
        }
        catch (IOException ioe)
        {
            log.error("error in sheduleExTraction:" + ioe);
        }
        return scheduleId;
    }

    private String buildScheduleRequestJson()
    {
        ScheduleRequestJson scheduleRequestJson = new ScheduleRequestJson();
        DatascopeEndPointConfig config = client.config();
        scheduleRequestJson.setName("AcuoFXImmediateSchedule_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss")));
        scheduleRequestJson.setListId(config.getListId());
        scheduleRequestJson.setReportTemplateId(config.getReportTemplateId());
        Recurrence recurrence = new Recurrence();
        recurrence.setOdataType("#ThomsonReuters.Dss.Api.Extractions.Schedules.SingleRecurrence");
        recurrence.setIsimmediate(true);
        scheduleRequestJson.setRecurrence(recurrence);
        Trigger trigger = new Trigger();
        trigger.setOdataType("#ThomsonReuters.Dss.Api.Extractions.Schedules.ImmediateTrigger");
        trigger.setLimitReportToTodaysData(true);
        scheduleRequestJson.setTrigger(trigger);

        String value = null;
        try
        {
            value = objectMapper.writeValueAsString(scheduleRequestJson);
        }
        catch (JsonProcessingException jpe)
        {
            log.error("error in schedule json :" + jpe);
        }
        log.info("request json:" + value);
        return value;
    }
}
