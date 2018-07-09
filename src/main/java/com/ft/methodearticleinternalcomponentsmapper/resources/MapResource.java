package com.ft.methodearticleinternalcomponentsmapper.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodearticleinternalcomponentsmapper.exception.InvalidMethodeContentException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleUnsupportedSourceCodeException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper;
import org.apache.http.HttpStatus;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Path("/")
public class MapResource {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

    private final InternalComponentsMapper internalComponentsMapper;

    public MapResource(InternalComponentsMapper internalComponentsMapper) {
        this.internalComponentsMapper = internalComponentsMapper;
    }

    @POST
    @Timed
    @QueryParam("preview")
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final InternalComponents map(@QueryParam("preview") boolean preview, EomFile eomFile,
                                        @Context HttpHeaders httpHeaders) {

        String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders);
        try {
            return internalComponentsMapper.map(eomFile, transactionId, new Date(), preview);
        } catch (MethodeMarkedDeletedException e) {
            throw new WebApplicationException(HttpStatus.SC_NOT_FOUND);
        } catch (MethodeArticleNotEligibleForPublishException | InvalidMethodeContentException
                | MethodeArticleUnsupportedSourceCodeException e) {
            throw new WebApplicationException(HttpStatus.SC_UNPROCESSABLE_ENTITY);
        } catch (TransformationException e) {
            throw new WebApplicationException(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}