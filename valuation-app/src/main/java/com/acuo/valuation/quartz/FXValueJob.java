package com.acuo.valuation.quartz;

import com.acuo.valuation.providers.datascope.service.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
                             DatascopePersistService datascopePersistService)
    {
        this.datascopeAuthService = datascopeAuthService;
        this.datascopeScheduleService = datascopeScheduleService;
        this.datascopeDownloadService = datascopeDownloadService;
        this.datascopeExtractionService = datascopeExtractionService;
        this.datascopePersistService = datascopePersistService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String token = datascopeAuthService.getToken();
        String scheduleId = datascopeScheduleService.scheduleFXRateExtraction(token);
        List<String> ids = datascopeExtractionService.getExtractionFileId(token, scheduleId);
        String csv = datascopeDownloadService.downloadFile(token, ids.get(1));
        BufferedReader br = new BufferedReader(new StringReader(csv));
        List<String> lines = new ArrayList<>();
        try
        {
            //skipe the first two line
            br.readLine();
            br.readLine();
            String line = null;
            while ((line= br.readLine())!=null)
            {
                lines.add(line);
            }

        }
        catch (Exception e)
        {
            log.error("error in getFx :" + e);
        }
        datascopePersistService.persistFxRate(lines);

    }
}
