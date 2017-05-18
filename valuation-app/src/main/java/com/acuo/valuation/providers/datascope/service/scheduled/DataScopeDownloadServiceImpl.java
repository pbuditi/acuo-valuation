package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DataScopeDownloadServiceImpl implements DataScopeDownloadService {

    private final ClientEndPoint<DataScopeEndPointConfig> client;

    @Inject
    public DataScopeDownloadServiceImpl(ClientEndPoint<DataScopeEndPointConfig> client)
    {
        this.client = client;
    }
    public String downloadFile(String token, String extractedFileId)
    {
        String response = DataScopeDownloadCall.of(client).with("token", token).with("id", extractedFileId).create().send();
        log.info(response);
        return response;
    }
}
