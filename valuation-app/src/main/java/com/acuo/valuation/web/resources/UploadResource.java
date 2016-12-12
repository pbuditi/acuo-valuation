package com.acuo.valuation.web.resources;

import com.acuo.valuation.services.IRSService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/upload")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Slf4j
public class UploadResource {

    private final IRSService irsService;

    @Inject
    public UploadResource(IRSService irsService) {
        this.irsService = irsService;
    }

    @POST
    @Path("/upload")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input) {

        String fileName = "";

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (InputPart inputPart : inputParts) {

            try {

                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);

                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                irsService.uploadIRS(inputStream);

            } catch (IOException e) {
               log.error("error occured when loading {}", fileName, e);
            }

        }

        return Response.status(200)
                .entity("uploadFile is called, Uploaded file name : " + fileName).build();

    }

    /**
     * header sample
     * {
     * Content-Type=[image/png],
     * Content-Disposition=[form-data; name="file"; filename="filename.extension"]
     * }
     **/
    //get uploaded filename, is there a easy way in RESTEasy?
    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

}
