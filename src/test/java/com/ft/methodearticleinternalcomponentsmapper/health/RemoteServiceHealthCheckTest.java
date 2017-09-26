package com.ft.methodearticleinternalcomponentsmapper.health;

import com.ft.platform.dropwizard.AdvancedResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteServiceHealthCheckTest {

    private static String HOST = "localhost";
    private static int PORT = 9080;
    private static String HOST_HEADER = "upp-service-header";

    private RemoteServiceHealthCheck healthCheck;

    @Mock
    private Client mockClient;
    @Mock
    private WebResource mockResource;
    @Mock
    private ClientResponse mockClientResponse;

    @Before
    public void setUp() throws Exception {

        URI goodToGoUri = UriBuilder.fromPath("__gtg")
                .scheme("http").host(HOST).port(PORT).build();
        when(mockClient.resource(goodToGoUri)).thenReturn(mockResource);
        WebResource.Builder builder = mock(WebResource.Builder.class);
        when(mockResource.header("Host", HOST_HEADER)).thenReturn(builder);
        when(builder.get(ClientResponse.class)).thenReturn(mockClientResponse);


        healthCheck = new RemoteServiceHealthCheck(
                "upp-service",
                mockClient,
                HOST,
                PORT,
                "/__gtg",
                HOST_HEADER,
                1,
                "very high impact!",
                "http://a.panic.guide.url/"
        );
    }

    @Test
    public void testWhenUppServiceIsUpHealthCheckShouldPass() throws Exception {
        when(mockClientResponse.getStatus()).thenReturn(200);

        AdvancedResult expectedHealthCheckResult = AdvancedResult.healthy();
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }

    @Test
    public void testWhenUppServiceReturnsUnexpectedStatusHealthCheckShouldFail() throws Exception {
        when(mockClientResponse.getStatus()).thenReturn(503);

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthCheck, "Unexpected status : 503");
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }

    @Test
    public void testWhenUppServiceIsDownHealthCheckShouldFail() throws Exception {
        when(mockResource.get(ClientResponse.class)).thenThrow(new ClientHandlerException("timeout"));

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthCheck, "Methode api ping: Exception during ping, timeout");
        AdvancedResult actualHealthCheckResult = healthCheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
    }
}
