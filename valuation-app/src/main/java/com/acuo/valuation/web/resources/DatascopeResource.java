package com.acuo.valuation.web.resources;

import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeDownloadService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopePersistService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeExtractionService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeScheduleService;
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


    private final DataScopeAuthService dataScopeAuthService;

    private final DataScopeScheduleService dataScopeScheduleService;

    private final DataScopeExtractionService dataScopeExtractionService;

    private final DataScopeDownloadService dataScopeDownloadService;

    private final DataScopePersistService dataScopePersistService;

    @Inject
    public DatascopeResource(DataScopeAuthService dataScopeAuthService,
                             DataScopeScheduleService dataScopeScheduleService,
                             DataScopeExtractionService dataScopeExtractionService,
                             DataScopeDownloadService dataScopeDownloadService,
                             DataScopePersistService dataScopePersistService)
    {
        this.dataScopeAuthService = dataScopeAuthService;
        this.dataScopeScheduleService = dataScopeScheduleService;
        this.dataScopeDownloadService = dataScopeDownloadService;
        this.dataScopeExtractionService = dataScopeExtractionService;
        this.dataScopePersistService = dataScopePersistService;
    }


    @GET
    @Path("/getFX")
    @Timed
    public Response getFx()
    {
        String token = dataScopeAuthService.getToken();
        String scheduleId = dataScopeScheduleService.scheduleFXRateExtraction(token);
        List<String> ids = dataScopeExtractionService.getExtractionFileId(token, scheduleId);
        String csv = dataScopeDownloadService.downloadFile(token, ids.get(1));
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
        dataScopePersistService.persistFxRate(lines);
        return Response.ok().build();
    }

    @GET
    @Path("/getBond")
    @Timed
    public Response getBond()
    {
        String token = dataScopeAuthService.getToken();
        String scheduleId = dataScopeScheduleService.scheduleBondExtraction(token);
        List<String> ids = dataScopeExtractionService.getExtractionFileId(token, scheduleId);
        String csv = dataScopeDownloadService.downloadFile(token, ids.get(1));
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
        dataScopePersistService.persistBond(lines);
        return Response.ok().build();
    }
}
