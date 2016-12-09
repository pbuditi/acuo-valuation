package com.acuo.valuation.web.resources;

import com.acuo.common.security.EncryptionModule;
import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.persist.modules.Neo4jPersistModule;
import com.acuo.valuation.modules.*;
import com.acuo.valuation.web.JacksonObjectMapperProvider;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ConfigurationTestModule.class, EncryptionModule.class, Neo4jPersistModule.class, MappingModule.class, EndPointModule.class, ServicesModule.class, ResourcesModule.class})
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
    public void testUploadExcelFile() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("/upload");
        request.contentType(MediaType.APPLICATION_OCTET_STREAM);
        request.content(excel.getInputStream());
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertThat(response.getContentAsString()).isNotNull();
    }
}
