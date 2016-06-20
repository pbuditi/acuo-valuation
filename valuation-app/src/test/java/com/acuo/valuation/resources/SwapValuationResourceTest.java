package com.acuo.valuation.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.acuo.common.util.ResourceFile;
import com.acuo.valuation.web.MOXyCustomJsonProvider;

public class SwapValuationResourceTest {

	@Rule
	public ResourceFile sample = new ResourceFile("/requests/markit-sample.json");

	Dispatcher dispatcher;

	@Before
	public void setup() {
		SwapValuationResource resource = new SwapValuationResource();
		dispatcher = createDispatcher();
		dispatcher.getRegistry().addSingletonResource(resource);
	}

	@Test
	public void testSwapValuation() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.post("/swaps/value");
		MockHttpResponse response = new MockHttpResponse();

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(sample.getInputStream());

		dispatcher.invoke(request, response);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertThat(response.getContentAsString()).isNotNull();
	}

	public static Dispatcher createDispatcher() {
		ResteasyProviderFactory.getInstance().registerProvider(MOXyCustomJsonProvider.class);
		Dispatcher dispatcher = new SynchronousDispatcher(ResteasyProviderFactory.getInstance());
		ResteasyProviderFactory.setInstance(dispatcher.getProviderFactory());
		RegisterBuiltin.register(dispatcher.getProviderFactory());
		return dispatcher;
	}
}
