package com.acuo.valuation.quartz;

import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeDownloadService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeExtractionService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopePersistService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeScheduleService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class FXScheduledValueJob implements Job {

    private final DataScopeAuthService dataScopeAuthService;
    private final DataScopeScheduleService dataScopeScheduleService;
    private final DataScopeExtractionService dataScopeExtractionService;
    private final DataScopeDownloadService dataScopeDownloadService;
    private final DataScopePersistService dataScopePersistService;

    private final static boolean staticFile = true;

    @Inject
    public FXScheduledValueJob(DataScopeAuthService dataScopeAuthService,
                               DataScopeScheduleService dataScopeScheduleService,
                               DataScopeExtractionService dataScopeExtractionService,
                               DataScopeDownloadService dataScopeDownloadService,
                               DataScopePersistService dataScopePersistService) {
        this.dataScopeAuthService = dataScopeAuthService;
        this.dataScopeScheduleService = dataScopeScheduleService;
        this.dataScopeDownloadService = dataScopeDownloadService;
        this.dataScopeExtractionService = dataScopeExtractionService;
        this.dataScopePersistService = dataScopePersistService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting fx rate scheduled job");
        try {
            List<String> files = (staticFile) ? staticFiles() : remoteFiles();
            List<String> lines = files.stream()
                    .skip(1)
                    .limit(1)
                    .flatMap(source -> {
                        try (BufferedReader reader = new BufferedReader(new StringReader(source))) {
                            return reader.lines().skip(2).collect(toList()).stream();
                        } catch (IOException e) {
                            log.error("error in getFx :" + e);
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(toList());
            dataScopePersistService.persistFxRate(lines);
            log.info("fx rates service job complete with {} rates", lines.size());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    private List<String> staticFiles() throws IOException, URISyntaxException {
        String file = readFile("/fx/rates.csv");
        return ImmutableList.of("", file);
    }

    private List<String> remoteFiles() {
        String token = dataScopeAuthService.getToken();
        String scheduleId = dataScopeScheduleService.scheduleFXRateExtraction(token);
        List<String> ids = dataScopeExtractionService.getExtractionFileId(token, scheduleId);
        return ids.stream()
                .map(id -> dataScopeDownloadService.downloadFile(token, id))
                .peek(s -> {
                    if (log.isDebugEnabled()) {
                        log.debug("extracted file {}", s);
                    }
                })
                .collect(toList());

    }

    private static String readFile(String filePath) throws IOException, URISyntaxException {
        return IOUtils.toString(FXScheduledValueJob.class.getResourceAsStream(filePath), StandardCharsets.UTF_8);
    }
}
