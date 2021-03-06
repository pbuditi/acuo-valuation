package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.core.ImportService;
import com.acuo.persist.modules.*;
import com.acuo.valuation.modules.ConfigurationTestModule;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({
        ConfigurationTestModule.class,
        EncryptionModule.class,
        Neo4jPersistModule.class,
        DataImporterModule.class,
        DataLoaderModule.class,
        ImportServiceModule.class,
        RepositoryModule.class,
        MappingModule.class,
        EndPointModule.class,
        ServicesModule.class,
        ResourcesModule.class})
@Slf4j
@Ignore
public class UploadResourceTest implements WithResteasyFixtures {

    private Dispatcher dispatcher;

    @Rule
    public ResourceFile excel = new ResourceFile("/excel/TradePortfolio.xlsx");

    @Rule
    public ResourceFile one = new ResourceFile("/excel/OneIRS.xlsx");


    @Rule
    public ResourceFile generatedllMC = new ResourceFile("/acuo/margincalls/upload-all-mc.json");

    @Inject
    private ImportService importService = null;

    @Inject
    private UploadResource uploadResource = null;

    @Inject
    private SwapValuationResource swapValuationResource = null;

    @Before
    public void setup() throws IOException {
        dispatcher = createDispatcher(JacksonObjectMapperProvider.class);
        dispatcher.getRegistry().addSingletonResource(uploadResource);
        dispatcher.getRegistry().addSingletonResource(swapValuationResource);
        importService.reload();
    }

    @Test
    public void testUploadBigFile() throws URISyntaxException, IOException {
        testFileUpload(excel);
    }

    @Test
    public void testUploadBigFileNew() throws URISyntaxException, IOException {
        testFileUploadNew(excel);
    }

    @Test
    public void testUploadSmallFileNew() throws URISyntaxException, IOException {
        testFileUploadNew(one);
    }

    @Test
    public void testUploadSmallFileMultipleTimes() throws URISyntaxException, IOException {
        testFileUpload(one);
        testFileUpload(one);
    }

    private void testFileUploadNew(ResourceFile resourceFile) throws IOException, URISyntaxException {

        Map<String, Object> parts = new HashMap<>();
        String charSet = "ISO-8859-1";
        parts.put("file", resourceFile.getContent(charSet));
        MockHttpRequest request = multipartRequest("/upload/v1", parts, charSet);

        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertThat(response.getContentAsString())
                .isNotNull();
        log.info(response.getContentAsString());
    }

    private void testFileUpload(ResourceFile resourceFile) throws IOException, URISyntaxException {

        Map<String, Object> parts = new HashMap<>();
        String charSet = "ISO-8859-1";
        parts.put("file", resourceFile.getContent(charSet));
        MockHttpRequest request = multipartRequest("/upload", parts, charSet);

        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertThat(response.getContentAsString())
                .isNotNull();
        log.info(response.getContentAsString());
    }


    /**
     * Return a multipart/form-data MockHttpRequest
     *
     * @param parts Key is the name of the part, value is either a String or a File.
     * @return request
     */
    private MockHttpRequest multipartRequest(String uri, Map<String, Object> parts, String charSet) throws URISyntaxException, IOException {
        MockHttpRequest req = MockHttpRequest.post(uri);
        String boundary = UUID.randomUUID().toString();
        req.contentType("multipart/form-data; boundary=" + boundary);

        StringWriter writer = new StringWriter();
        writer.append("--").append(boundary);

        Set<Map.Entry<String, Object>> set = parts.entrySet();
        for (Map.Entry entry : set) {
            if (entry.getValue() instanceof String) {
                writer.append("\n");
                writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey().toString()).append("\"\n\n");
                writer.append(entry.getValue().toString()).append("\n");
                writer.append("--").append(boundary);
            } else if (entry.getValue() instanceof InputStream) {
                writer.append("\n");
                InputStream val = (InputStream) entry.getValue();
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"file.bin\"").append("\n");
                writer.append("Content-Type: application/octet-stream").append("\n\n");
                int b = val.read();
                while (b >= 0) {
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
        byte[] bytes = body.getBytes(charSet);
        req.content(bytes);
        return req;
    }
}
