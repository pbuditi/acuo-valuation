package com.acuo.valuation.resources;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.acuo.common.util.GuiceJUnitRunner;
import com.acuo.common.util.GuiceJUnitRunner.GuiceModules;
import com.acuo.common.util.ResourceFile;
import com.acuo.common.util.WithResteasyFixtures;
import com.acuo.valuation.modules.ResourcesModule;
import com.acuo.valuation.requests.dto.SwapDTO;
import com.acuo.valuation.resources.SwapValuationResourceTest.MockServiceModule;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.Result;
import com.acuo.valuation.web.MOXyCustomJsonProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ MockServiceModule.class, ResourcesModule.class })
public class SwapValuationResourceTest implements WithResteasyFixtures {

	@Rule
	public ResourceFile sample = new ResourceFile("/requests/markit-sample.json");

	@Rule
	public ResourceFile swap = new ResourceFile("/requests/dto-swap.json");

	public static class MockServiceModule extends AbstractModule {

		@Mock
		PricingService pricingService;
		@Mock
		Result result;

		public MockServiceModule() {
			MockitoAnnotations.initMocks(this);

			when(pricingService.price(any(SwapDTO.class))).thenReturn(result);
			when(result.toString()).thenReturn("Result");
		}

		@Override
		protected void configure() {
			bind(PricingService.class).toInstance(pricingService);
		}

	}

	Dispatcher dispatcher;

	@Inject
	SwapValuationResource resource;

	@Before
	public void setup() {
		dispatcher = createDispatcher(MOXyCustomJsonProvider.class);
		dispatcher.getRegistry().addSingletonResource(resource);
	}

	@Test
	public void testWelcomPage() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get("/swaps");
		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertThat(response.getContentAsString()).isNotNull();
	}

	@Test
	public void testSwapValuation() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.post("/swaps/value");
		MockHttpResponse response = new MockHttpResponse();

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(swap.getInputStream());

		dispatcher.invoke(request, response);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertThat(response.getContentAsString()).isNotNull();
		assertThatJson(response.getContentAsString()).isEqualTo("{}");
	}
}
