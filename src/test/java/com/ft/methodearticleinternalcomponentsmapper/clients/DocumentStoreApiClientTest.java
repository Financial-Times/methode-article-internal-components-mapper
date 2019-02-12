package com.ft.methodearticleinternalcomponentsmapper.clients;

import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiInvalidRequestException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnavailableException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnmarshallingException;
import com.ft.methodearticleinternalcomponentsmapper.model.Content;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreApiClientTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String HOST_HEADER = "document-store-api";
    private static final String UUID = "fbbee07f-5054-4a42-b596-64e0625d19a6";
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Mock
    private Client jerseyClient;
    @Mock
    private WebResource webResource;
    @Mock
    private WebResource.Builder webResourceBuilder;
    @Mock
    private ClientResponse clientResponse;
    private DocumentStoreApiClient documentStoreApiClient;

    @Before
    public void setUp() {
        documentStoreApiClient = new DocumentStoreApiClient(jerseyClient, "localhost", 8080, HOST_HEADER);

        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.type(MediaType.APPLICATION_JSON_TYPE)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.header("Host", HOST_HEADER)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.post(eq(ClientResponse.class), anyList())).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
    }

    @Test
    public void testGetContentForUuidsReturnsEmptyListIfUuidsIsEmpty() {
        List<Content> content = documentStoreApiClient.getContentForUuids(Collections.emptyList(), TRANSACTION_ID);
        assertThat(content).isEmpty();
    }

    @Test(expected = DocumentStoreApiUnavailableException.class)
    public void testGetContentForUuidsThrowsExceptionIfDocumentStoreReturns5xx() {
        when(clientResponse.getStatus()).thenReturn(500);
        documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);
    }

    @Test(expected = DocumentStoreApiInvalidRequestException.class)
    public void testGetContentForUuidsThrowsExceptionIfDocumentStoreReturns4xx() {
        when(clientResponse.getStatus()).thenReturn(400);
        documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);
    }

    @Test(expected = ClientHandlerException.class)
    public void testGetContentForUuidsThrowsExceptionIfClientThrowsClientHandlerException() {
        when(webResourceBuilder.post(eq(ClientResponse.class), anyList())).thenThrow(new ClientHandlerException());
        documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);
    }

    @Test(expected = DocumentStoreApiUnavailableException.class)
    public void testGetContentForUuidsThrowsExceptionIfClientThrowsClientHandlerExceptionWithIOException() {
        when(webResourceBuilder.post(eq(ClientResponse.class), anyList())).thenThrow(new ClientHandlerException(new IOException()));
        documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);
    }

    @Test(expected = DocumentStoreApiUnmarshallingException.class)
    public void testGetContentForUuidsThrowsExceptionIfItFailsToUnmarshallResponse() {
        when(clientResponse.getEntity(String.class)).thenReturn("not a json");
        documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);
    }

    @Test
    public void testGetContentForUuidsReturnsValidContentList() {
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + UUID + "\", \"type\": \"Article\"}]");
        List<Content> content = documentStoreApiClient.getContentForUuids(Collections.singletonList(UUID), TRANSACTION_ID);

        assertThat(content).isNotNull();
        assertThat(content.size()).isEqualTo(1);
        assertThat(content).contains(new Content(UUID, "Article"));
    }

    @Test
    public void testIsUUIDPresent_ReturnsTrueWhenUUIDIsPresentInDocumentStore() {
        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.get(eq(ClientResponse.class))).thenReturn(clientResponse);

        boolean isPresent = documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);

        assertTrue(isPresent);
    }

    @Test
    public void testIsUUIDPresent_ReturnsFalseWhenUUIDIsNotPresentInDocumentStore() {
        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.get(eq(ClientResponse.class))).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(404);

        boolean isPresent = documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);

        assertFalse(isPresent);
    }

    @Test
    public void testIsUUIDPresent_ThrowsExceptionWhenDocumentStoreReturnsUnexpectedStatus() {
        when(jerseyClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.header(TRANSACTION_ID_HEADER, TRANSACTION_ID)).thenReturn(webResourceBuilder);
        when(webResourceBuilder.get(eq(ClientResponse.class))).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(400);

        exception.expect(DocumentStoreApiException.class);
        documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);

        when(clientResponse.getStatus()).thenReturn(500);

        exception.expect(DocumentStoreApiException.class);
        documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);

        when(clientResponse.getStatus()).thenReturn(201);

        exception.expect(DocumentStoreApiException.class);
        documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);

        when(clientResponse.getStatus()).thenReturn(300);

        exception.expect(DocumentStoreApiException.class);
        documentStoreApiClient.isUUIDPresent(UUID, TRANSACTION_ID);
    }
}
