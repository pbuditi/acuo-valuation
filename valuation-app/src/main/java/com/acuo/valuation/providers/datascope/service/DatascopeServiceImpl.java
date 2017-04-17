package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.protocol.auth.AuthJson;
import com.acuo.valuation.providers.datascope.protocol.auth.Credentials;
import com.acuo.valuation.providers.datascope.protocol.auth.TokenJson;
import com.acuo.valuation.providers.datascope.protocol.schedule.Recurrence;
import com.acuo.valuation.providers.datascope.protocol.schedule.ScheduleRequestJson;
import com.acuo.valuation.providers.datascope.protocol.schedule.ScheduleResponseJson;
import com.acuo.valuation.providers.datascope.protocol.schedule.Trigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DatascopeServiceImpl implements DatascopeService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final ObjectMapper objectMapper;

    private String token;

    @Inject
    public DatascopeServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        objectMapper = new ObjectMapper();
    }

    public String getToken()
    {
        String response = DatascopeAuthCall.of(client).with("josn", buildAuthJson()).create().send();
        try
        {
            token = objectMapper.readValue(response, TokenJson.class).getValue();
        }
        catch (IOException ioe)
        {
            log.error("error in getToekn:" + ioe);
        }
        log.info("token is :" + token);
        return token;
    }

    public String sheduleExTraction()
    {
        String scheduleId = null;
        if(token == null)
            getToken();
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

    private String buildAuthJson()
    {
        DatascopeEndPointConfig config = client.config();
        Credentials credentials = new Credentials();
        credentials.setUsername(config.getUsername());
        credentials.setPassword(config.getPassword());
        AuthJson authJson = new AuthJson();
        authJson.setCredentials(credentials);

        String value= null;
        try
        {
            value = objectMapper.writeValueAsString(authJson);
        }
        catch (JsonProcessingException jpe)
        {
            log.error("error in auth json :" + jpe);
        }
        return value;
    }
}
