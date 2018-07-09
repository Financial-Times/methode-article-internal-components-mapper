package com.ft.methodearticleinternalcomponentsmapper.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleInternalComponentsMapperException;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.time.ZoneOffset.UTC;

public class MessageBuilder {

    private static final String CMS_CONTENT_PUBLISHED = "cms-content-published";
    private static final DateTimeFormatter RFC3339_FMT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
    private static final String TYPE_BASE_URL = "http://www.ft.com/ontology/content/";

    private final UriBuilder contentUriBuilder;
    private final SystemId systemId;
    private final ObjectMapper objectMapper;


    public MessageBuilder(UriBuilder contentUriBuilder, String systemId, ObjectMapper objectMapper) {
        this.contentUriBuilder = contentUriBuilder;
        this.systemId = SystemId.systemIdFromCode(systemId);
        this.objectMapper = objectMapper;
    }

    Message buildMessage(InternalComponents internalComponents) {
        MessageBody msgBody = new MessageBody(
                internalComponents,
                contentUriBuilder.build(internalComponents.getUuid()).toString(),
                RFC3339_FMT.format(OffsetDateTime.ofInstant(internalComponents.getLastModified().toInstant(), UTC)),
                TYPE_BASE_URL + internalComponents.getType()
        );

        return buildMessage(internalComponents.getUuid(), internalComponents.getPublishReference(), msgBody);
    }

    Message buildDeletedInternalComponentsMessage(String uuid, String publishReference, Date lastModified, String contentType) {
        MessageBody msgBody = new MessageBody(
                null,
                contentUriBuilder.build(uuid).toString(),
                RFC3339_FMT.format(OffsetDateTime.ofInstant(lastModified.toInstant(), UTC)),
                TYPE_BASE_URL + contentType
        );
        return buildMessage(uuid, publishReference, msgBody);
    }

    private Message buildMessage(String uuid, String publishReference, MessageBody msgBody) {
        Message msg;
        try {
            String messageBody = objectMapper.writeValueAsString(msgBody);
            msg = new Message.Builder().withMessageId(UUID.randomUUID())
                    .withMessageType(CMS_CONTENT_PUBLISHED)
                    .withMessageTimestamp(new Date())
                    .withOriginSystemId(systemId)
                    .withContentType("application/json")
                    .withMessageBody(messageBody)
                    .build();

            msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, publishReference);
            msg = KeyedMessage.forMessageAndKey(msg, uuid);
        } catch (JsonProcessingException e) {
            throw new MethodeArticleInternalComponentsMapperException("unable to write JSON for message", e);
        }
        return msg;
    }

    public static class MessageBody {
        @JsonProperty("payload")
        public final InternalComponents payload;
        @JsonProperty("contentUri")
        public final String contentUri;
        @JsonProperty("lastModified")
        public final String lastModified;
        @JsonProperty("contentType")
        public final String contentType;

        MessageBody(
                @JsonProperty("payload")
                        InternalComponents payload,
                @JsonProperty("contentUri")
                        String contentUri,
                @JsonProperty("lastModified")
                        String lastModified,
                @JsonProperty("contentType")
                        String contentType) {
            this.contentUri = contentUri;
            this.payload = payload;
            this.lastModified = lastModified;
            this.contentType = contentType;
        }
    }
}
