package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.ClientEndPoint;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DatascopeDownloadServiceImpl implements DatascopeDownloadService {

    private final ClientEndPoint<DatascopeEndPointConfig> client;

    @Inject
    public DatascopeDownloadServiceImpl(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
    }
    public String downloadFile(String token, String extractedFileId)
    {
        String response = DatascopeDownloadCall.of(client).with("token", token).with("id", extractedFileId).create().send();
        log.info(response);
        return response;
    }
}
