package com.ft.methodearticleinternalcomponentsmapper.resources;

import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class MapResourceTest {

    private static final String TRANSACTION_ID = "tid_test";

    private HttpHeaders httpHeaders = mock(HttpHeaders.class);
    private InternalComponentsMapper internalComponentsMapper = mock(InternalComponentsMapper.class);
    private EomFile eomFile = mock(EomFile.class);
    private UUID uuid = UUID.randomUUID();
    private String type = "DynamicContent";

    private MapResource mapResource = new MapResource(internalComponentsMapper);

    @Before
    public void preconditions() throws Exception {
        when(eomFile.getUuid()).thenReturn(uuid.toString());
        when(httpHeaders.getRequestHeader(TransactionIdUtils.TRANSACTION_ID_HEADER)).thenReturn(Arrays.asList(TRANSACTION_ID));
    }

    @Test
    public void thatMapPreviewProcessedOk() {
        mapResource.map(true, eomFile, httpHeaders);
        verify(internalComponentsMapper, times(1)).map(eq(eomFile), eq(TRANSACTION_ID), any(), eq(true));
    }

    @Test
    public void thatMapPublicationProcessedOk() {
        mapResource.map(false, eomFile, httpHeaders);
        verify(internalComponentsMapper, times(1)).map(eq(eomFile), eq(TRANSACTION_ID), any(), eq(false));
    }

    @Test
    public void shouldThrow404ExceptionWhenContentIsMarkedAsDeletedInMethode() {

        when(internalComponentsMapper.map(eq(eomFile), eq(TRANSACTION_ID), any(), anyBoolean()))
                .thenThrow(new MethodeMarkedDeletedException(uuid, type));
        try {
            mapResource.map(false, eomFile, httpHeaders);
            fail("No exception was thrown, but expected one.");
        } catch (WebApplicationException wace) {
            assertThat(wace.getResponse().getStatus(), equalTo(HttpStatus.SC_NOT_FOUND));
        }
    }

    @Test
    public void shouldThrow422ExceptionWhenPublicationNotEligibleForPublishing() {

        when(internalComponentsMapper.map(eq(eomFile), eq(TRANSACTION_ID), any(), anyBoolean())).
                thenThrow(new MethodeArticleNotEligibleForPublishException(uuid));
        try {
            mapResource.map(false, eomFile, httpHeaders);
            fail("No exception was thrown, but expected one.");
        } catch (WebApplicationException wace) {
            assertThat(wace.getResponse().getStatus(), equalTo(HttpStatus.SC_UNPROCESSABLE_ENTITY));
        }
    }
}
