package com.acuo.valuation.util;

import com.acuo.common.util.ResourceFile;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@Ignore
public class TestDynamicJAXB {

    @Rule
    public ResourceFile sample = new ResourceFile("/markit/requests/markit-sample.xml");

    @Rule
    public ResourceFile schema = new ResourceFile("/markit/requests/PresentValue.xsd");

    @Test
    public void verifyContext() throws Exception {
        DynamicJAXBContext jaxbContext = createContext();

        assertNotNull(jaxbContext);
    }

    @Test
    public void verifyReadFromAdminServer() throws Exception {
        DynamicJAXBContext jaxbContext = createContext();

        DynamicEntity allLeagues = (DynamicEntity) jaxbContext.createUnmarshaller().unmarshal(sample.getInputStream());

        assertNotNull(allLeagues);
        List<DynamicEntity> leagues = allLeagues.<List<DynamicEntity>>get("league");
        assertNotNull(leagues);

        for (DynamicEntity league : leagues) {
            System.out.println("League(" + league.<String>get("id") + ", " + league.get("value") + ")");
        }
    }

    private DynamicJAXBContext createContext() throws IOException, JAXBException {
        InputStream in = schema.getInputStream();
        try {
            return DynamicJAXBContextFactory.createContextFromXSD(in, null, this.getClass().getClassLoader(), null);
        } finally {
            in.close();
        }

    }
}