package com.ft.methodearticleinternalcomponentsmapper.validation;

import com.ft.jerseyhttpwrapper.ResilientClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMapperUnavailableException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MethodeArticleValidatorTest {

    @Mock
    private ResilientClient methodeArticleMapperClient;

    @Mock
    private WebResource.Builder builder;
    @Mock
    private InBoundHeaders headers;
    @Mock
    private MessageBodyWorkers workers;

    @Mock
    private EomFile eomFile;

    private InputStream entity;

    @Before
    public void setup() throws Exception {

        entity = new ByteArrayInputStream("Test".getBytes(StandardCharsets.UTF_8));
        WebResource webResource = mock(WebResource.class);
        when(methodeArticleMapperClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.queryParam(eq("preview"), anyString())).thenReturn(webResource);
        when(webResource.accept(any(MediaType.class))).thenReturn(builder);
        when(builder.type(any(MediaType.class))).thenReturn(builder);
        when(builder.header(anyString(), anyObject())).thenReturn(builder);
        when(builder.entity(anyObject())).thenReturn(builder);
        when(builder.post(ClientResponse.class)).thenReturn(clientResponseWithCode(404));

        methodeArticleValidator = new MethodeArticleValidator(
                methodeArticleMapperClient,
                URI.create("http://localhost:8080/__methode-article-mapper/map"),
                "methode-article-mapper"
        );
    }

    private MethodeArticleValidator methodeArticleValidator;

    private static final String TRANSACTION_ID = "tid_test";

    @Test
    public void thatIfValidatorServiceReturns422PublishStatusIsIneligible() {
        when(builder.post(ClientResponse.class)).thenReturn(clientResponseWithCode(422));

        PublishingStatus actual = methodeArticleValidator.getPublishingStatus(eomFile, TRANSACTION_ID, false);

        assertThat(actual, is(PublishingStatus.INELIGIBLE));
    }

    @Test
    public void thatIfValidatorServiceReturns404PublishStatusIsDeleted() {
        when(builder.post(ClientResponse.class)).thenReturn(clientResponseWithCode(404));

        PublishingStatus actual = methodeArticleValidator.getPublishingStatus(eomFile, TRANSACTION_ID, false);

        assertThat(actual, is(PublishingStatus.DELETED));
    }

    @Test
    public void thatIfValidatorServiceReturns200PublishStatusIsValid() {
        when(builder.post(ClientResponse.class)).thenReturn(clientResponseWithCode(200));

        PublishingStatus actual = methodeArticleValidator.getPublishingStatus(eomFile, TRANSACTION_ID, false);

        assertThat(actual, is(PublishingStatus.VALID));
    }

    @Test(expected = MethodeArticleMapperUnavailableException.class)
    public void thatIfValidatorServiceReturnsNonExpectedStatusCodeMethodeArticleMapperUnavailableExceptionIsThrown() {
        when(builder.post(ClientResponse.class)).thenReturn(clientResponseWithCode(500));
        methodeArticleValidator.getPublishingStatus(eomFile, TRANSACTION_ID, false);
    }

    private ClientResponse clientResponseWithCode(int status) {
        return new ClientResponse(status, headers, entity, workers);
    }
}