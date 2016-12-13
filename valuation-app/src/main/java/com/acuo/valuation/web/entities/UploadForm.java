package com.acuo.valuation.web.entities;

import lombok.Data;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;

@Data
public class UploadForm {

    @FormParam("myFile")
    @PartType("application/octet-stream")
    private byte[] file;

}