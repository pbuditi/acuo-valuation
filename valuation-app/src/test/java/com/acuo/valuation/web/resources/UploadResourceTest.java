package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, EncryptionModule.class, Neo4jPersistModule.class, MappingModule.class, EndPointModule.class, ServicesModule.class, ResourcesModule.class})
@Slf4j
@Ignore
public class UploadResourceTest implements WithResteasyFixtures {

    Dispatcher dispatcher;

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/NewExposures.xlsx");

    @Inject
    UploadResource resource;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(resource);
    }

    @Test
    public void testUploadExcelFile2() throws URISyntaxException, IOException {
        MockHttpRequest request = MockHttpRequest.post("/upload/test");
        request.contentType(MediaType.APPLICATION_OCTET_STREAM);
        byte[] bytes = excel.getContent().getBytes();
        request.content(excel.getInputStream());
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    @Test
    public void testUploadExcelFile() throws URISyntaxException, IOException {
        Map parts = new HashMap();
        parts.put("aFile", excel.getInputStream());
        MockHttpRequest request = multipartRequest("/upload/test2", parts);

        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }

    /**
     * Return a multipart/form-data MockHttpRequest
     * @param parts Key is the name of the part, value is either a String or a File.
     * @return
     */
    private MockHttpRequest multipartRequest(String uri, Map parts) throws URISyntaxException, IOException {
        MockHttpRequest req = MockHttpRequest.post(uri);
        String boundary = UUID.randomUUID().toString();
        req.contentType("multipart/form-data; boundary=" + boundary);

        File tmpMultipartFile = Files.createTempFile(null, null).toFile();
        System.out.println("Tmp file:" + tmpMultipartFile.getAbsolutePath());
        FileWriter writer = new FileWriter(tmpMultipartFile);
        writer.append("--").append(boundary);

        Set<Map.Entry> set = parts.entrySet();
        for(Map.Entry entry : set) {
            if(entry.getValue() instanceof String) {
                writer.append("\n");
                writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append("\n\n");
                writer.append(entry.getValue().toString()).append("\n");
                writer.append("--").append(boundary);
            } else if(entry.getValue() instanceof InputStream) {
                writer.append("\n");
                InputStream val = (InputStream) entry.getValue();
                writer.append("Content-Disposition: form-data; name=\"aFile\"; filename=\"file.bin\"").append("\n");
                writer.append("Content-Type: application/octet-stream").append("\n\n");

                int b = val.read();
                while(b >= 0) {
                    writer.write(b);
                    writer.flush();
                    b = val.read();
                }
                writer.append("\n").append("--").append(boundary);

            }
        }
        writer.append("--");
        writer.flush();
        writer.close();
        String body = writer.toString();
        log.debug(body);
        byte[] bytes = body.getBytes("UTF-8");
        req.content(bytes);
        return req;
    }

}
