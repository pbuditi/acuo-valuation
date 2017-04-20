package com.acuo.valuation.web.resources;

import com.acuo.valuation.providers.datascope.service.*;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Path("/datascope")
public class DatascopeResource {


    private final DatascopeAuthService datascopeAuthService;

    private final DatascopeScheduleService datascopeScheduleService;

    private final DatascopeExtractionService datascopeExtractionService;

    private final DatascopeDownloadService datascopeDownloadService;

    private final DatascopePersistService datascopePersistService;

    @Inject
    public DatascopeResource(DatascopeAuthService datascopeAuthService,
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


    @GET
    @Path("/getFX")
    @Timed
    public Response getFx()
    {
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
        return Response.ok().build();
    }

    @GET
    @Path("/getBond")
    @Timed
    public Response getBond()
    {
        String token = datascopeAuthService.getToken();
        String scheduleId = datascopeScheduleService.scheduleBondExtraction(token);
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
        //datascopePersistService.persistFxRate(lines);
        return Response.ok().build();
    }
}
