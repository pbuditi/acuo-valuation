package com.acuo.valuation.quartz;

import com.acuo.valuation.providers.datascope.service.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
public class FXValueJob implements Job {

    private final DatascopeAuthService datascopeAuthService;

    private final DatascopeScheduleService datascopeScheduleService;

    private final DatascopeExtractionService datascopeExtractionService;

    private final DatascopeDownloadService datascopeDownloadService;

    private final DatascopePersistService datascopePersistService;

    @Inject
    public FXValueJob(DatascopeAuthService datascopeAuthService,
                      DatascopeScheduleService datascopeScheduleService,
                      DatascopeExtractionService datascopeExtractionService,
                      DatascopeDownloadService datascopeDownloadService,
                      DatascopePersistService datascopePersistService) {
        this.datascopeAuthService = datascopeAuthService;
        this.datascopeScheduleService = datascopeScheduleService;
        this.datascopeDownloadService = datascopeDownloadService;
        this.datascopeExtractionService = datascopeExtractionService;
        this.datascopePersistService = datascopePersistService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting fx rate job");
        String token = datascopeAuthService.getToken();
        String scheduleId = datascopeScheduleService.scheduleFXRateExtraction(token);
        List<String> ids = datascopeExtractionService.getExtractionFileId(token, scheduleId);
        List<String> lines = ids.stream()
                .limit(1)
                .map(id -> datascopeDownloadService.downloadFile(token, id))
                .flatMap(source -> {
                    try (BufferedReader reader = new BufferedReader(new StringReader(source))) {
                        return reader.lines().skip(2).collect(toList()).stream();
                    } catch (IOException e) {
                        log.error("error in getFx :" + e);
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(toList());
        datascopePersistService.persistFxRate(lines);
        log.info("fx rates service job complete with {} rates", lines.size());
    }
}
