package com.ft.methodearticleinternalcomponentsmapper.clients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.methodearticleinternalcomponentsmapper.configuration.UppServiceConfiguration;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiInvalidRequestException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnavailableException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnmarshallingException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransientUuidResolverException;
import com.ft.methodearticleinternalcomponentsmapper.exception.UuidResolverException;
import com.ft.methodearticleinternalcomponentsmapper.model.Content;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import io.dropwizard.setup.Environment;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static com.sun.jersey.api.client.ClientResponse.Status.getFamilyByStatusCode;

public class DocumentStoreApiClient extends UppServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStoreApiClient.class);
    private static final String QUERY_PATH = "/content-query";
    private static final String CONTENT_PATH = "/content";

    public DocumentStoreApiClient(UppServiceConfiguration uppServiceConfiguration, Environment environment) {
        super(uppServiceConfiguration, environment);
        configureJersey();
    }

    public DocumentStoreApiClient(Client documentStoreJerseyClient, String docStoreHost, int docStorePort, String docStoreHostHeader) {
        super(documentStoreJerseyClient, docStoreHost, docStorePort, null, docStoreHostHeader);
        configureJersey();
    }

    public String resolveUUID(final String identifierAuthority, final String identifierValue, final String transactionId) {
        if (identifierAuthority == null || identifierValue == null) {
            throw new UuidResolverException("Neither the identifierAuthority nor identifierValue should be null!");
        }

        final URI queryUri;
        try {
            queryUri = UriBuilder.fromPath(QUERY_PATH).scheme("http").host(apiHost).port(apiPort)
                    .queryParam("identifierAuthority", URLEncoder.encode(identifierAuthority, "UTF-8"))
                    .queryParam("identifierValue", URLEncoder.encode(identifierValue, "UTF-8"))
                    .build();
        } catch (final UnsupportedEncodingException ex) {
            LOG.error("Failed to encode query params!", ex);
            throw new UuidResolverException(ex);
        }

        LOG.info("Call to Document Store API: {}", queryUri);
        final ClientResponse response = jerseyClient.resource(queryUri)
                .header("Host", hostHeader)
                .header(TRANSACTION_ID_HEADER, transactionId)
                .get(ClientResponse.class);

        return processResponse(response, resp -> {
            if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
                throw new TransientUuidResolverException("Failed to find uuid in Document Store API! QueryURI: " + queryUri, queryUri, identifierValue);
            }

            final URI redirectUrl = resp.getLocation();
            if (redirectUrl == null) {
                LOG.error("DS API could not find the required resource! DS Query URL [{}], Response [{}]", queryUri.toString(), resp.toString());
                throw new UuidResolverException("DS API could not find the required resource! DS Query URL [" + queryUri.toString() + "], Response [" + resp.toString() + "]");
            }

            final String uuid = lastPath(redirectUrl.getPath());
            LOG.info("UUID for [{} / {}] is [{}].", identifierAuthority, identifierValue, uuid);

            return uuid;
        });
    }

    public boolean isUUIDPresent(final String uuid, final String transactionId) {
        final URI contentUri = UriBuilder.fromPath(CONTENT_PATH).path(uuid).scheme("http").host(apiHost).port(apiPort).build();
        LOG.info("Call to Document Store API: {}", contentUri);
        final ClientResponse response = jerseyClient.resource(contentUri)
                .header(TRANSACTION_ID_HEADER, transactionId)
                .get(ClientResponse.class);

        return processResponse(response, resp -> {
            if (resp.getStatus() == HttpStatus.SC_OK) {
                return true;
            }
            if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
                return false;
            } else {
                throw new DocumentStoreApiException(String.format("Document Store API returned an unexpected status code: %s for uuid: %s.", response.getStatus(), uuid));
            }
        });
    }

    public List<Content> getContentForUuids(Collection<String> uuids, String transactionId) {
        if (uuids.isEmpty()) {
            return Collections.emptyList();
        }

        URI contentUri = UriBuilder.fromPath(CONTENT_PATH).scheme("http").host(apiHost).port(apiPort)
                .queryParam("mget", true).build();
        ClientResponse clientResponse = null;
        try {
            clientResponse = jerseyClient.resource(contentUri)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header(TRANSACTION_ID_HEADER, transactionId)
                    .header("Host", hostHeader)
                    .post(ClientResponse.class, uuids);

            int responseStatusCode = clientResponse.getStatus();
            Response.Status.Family statusFamily = getFamilyByStatusCode(responseStatusCode);

            if (statusFamily == Response.Status.Family.SERVER_ERROR) {
                String msg = String.format("Document Store API returned %s", responseStatusCode);
                throw new DocumentStoreApiUnavailableException(msg);
            } else if (statusFamily == Response.Status.Family.CLIENT_ERROR) {
                String msg = String.format("Document Store API returned %s", responseStatusCode);
                throw new DocumentStoreApiInvalidRequestException(msg);
            }

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonAsString = clientResponse.getEntity(String.class);
            Content[] returnedContent = mapper.readValue(jsonAsString, Content[].class);

            return Arrays.asList(returnedContent);
        } catch (ClientHandlerException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new DocumentStoreApiUnavailableException(e);
            }
            throw e;
        } catch (IOException e) {
            throw new DocumentStoreApiUnmarshallingException("Failed to parse content received from Document Store API", e);
        } finally {
            if (clientResponse != null) {
                clientResponse.close();
            }
        }
    }

    private void configureJersey() {
        // Hack to force http client to stop handling redirects. This needs to be changed to the new 'DW way' when we upgrade from v0.7.1
        ClientHandler handler = jerseyClient.getHeadHandler();
        while (handler instanceof ClientFilter) {
            handler = ((ClientFilter) handler).getNext();
        }

        if (handler instanceof ApacheHttpClient4Handler) {
            LOG.info("Reconfiguring underlying http client to stop handling redirects.");
            ApacheHttpClient4Handler apacheHandler = (ApacheHttpClient4Handler) handler;
            apacheHandler.getHttpClient().getParams().setParameter("http.protocol.handle-redirects", false);
        }
    }

    private String lastPath(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
