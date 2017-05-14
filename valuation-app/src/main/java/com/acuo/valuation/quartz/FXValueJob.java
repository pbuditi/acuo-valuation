package com.acuo.valuation.quartz;

import com.acuo.persist.utils.GraphData;
import com.acuo.valuation.providers.datascope.service.DatascopeAuthService;
import com.acuo.valuation.providers.datascope.service.DatascopeDownloadService;
import com.acuo.valuation.providers.datascope.service.DatascopeExtractionService;
import com.acuo.valuation.providers.datascope.service.DataScopePersistService;
import com.acuo.valuation.providers.datascope.service.DatascopeScheduleService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class FXValueJob implements Job {

    private final DatascopeAuthService datascopeAuthService;
    private final DatascopeScheduleService datascopeScheduleService;
    private final DatascopeExtractionService datascopeExtractionService;
    private final DatascopeDownloadService datascopeDownloadService;
    private final DataScopePersistService dataScopePersistService;

    @Inject
    public FXValueJob(DatascopeAuthService datascopeAuthService,
                      DatascopeScheduleService datascopeScheduleService,
                      DatascopeExtractionService datascopeExtractionService,
                      DatascopeDownloadService datascopeDownloadService,
                      DataScopePersistService dataScopePersistService) {
        this.datascopeAuthService = datascopeAuthService;
        this.datascopeScheduleService = datascopeScheduleService;
        this.datascopeDownloadService = datascopeDownloadService;
        this.datascopeExtractionService = datascopeExtractionService;
        this.dataScopePersistService = dataScopePersistService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting fx rate job");
        /*String token = datascopeAuthService.getToken();
        String scheduleId = datascopeScheduleService.scheduleFXRateExtraction(token);
        List<String> ids = datascopeExtractionService.getExtractionFileId(token, scheduleId);
        final List<String> files = ids.stream()
                .map(id -> datascopeDownloadService.downloadFile(token, id))
                .peek(s -> {
                    if (log.isDebugEnabled()) {
                        log.debug("extracted file {}", s);
                    }
                })
                .collect(toList());*/
        try {
            String path = readFile("/fx/rates.csv");
            log.info(path);
            List<String> files = ImmutableList.of("", IOUtils.toString(new URI(path), Charsets.UTF_8));
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

    private static String getDataLink(String dataImportLink) {
        if (dataImportLink.startsWith("file:/"))
            return dataImportLink;
        return "file://" + FXValueJob.class.getResource(dataImportLink).getFile();
    }

    private static String readFile(String filePath) throws IOException, URISyntaxException {
        String path = getDataLink(filePath);
        return IOUtils.toString(new URI(path), com.google.common.base.Charsets.UTF_8);
    }
}
