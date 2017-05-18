package com.acuo.valuation.providers.datascope.service.scheduled;

public interface DataScopeDownloadService {

    String downloadFile(String token, String extractedFileId);
}
