package com.ft.methodearticleinternalcomponentsmapper.messaging;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleHasNoInternalComponentsException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;

public class MessageProducingInternalComponentsMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducingInternalComponentsMapper.class);

    private final MessageBuilder messageBuilder;
    private final MessageProducer producer;
    private final InternalComponentsMapper internalComponentsMapper;

    public MessageProducingInternalComponentsMapper(
            MessageBuilder messageBuilder,
            MessageProducer producer,
            InternalComponentsMapper internalComponentsMapper) {

        this.messageBuilder = messageBuilder;
        this.producer = producer;
        this.internalComponentsMapper = internalComponentsMapper;
    }

    void mapInternalComponents(EomFile methodeContent, String transactionId, Date messageTimestamp) {
        Message message;
        try {
            message = messageBuilder.buildMessage(
                    internalComponentsMapper.map(methodeContent, transactionId, messageTimestamp, false)
            );
        } catch (MethodeArticleMarkedDeletedException | MethodeArticleHasNoInternalComponentsException e) {
            LOGGER.info("Internal components of article {} are missing. " +
                            "Message with deleted internal components event is created.",
                    methodeContent.getUuid());
            message = messageBuilder.buildDeletedInternalComponentsMessage(
                    methodeContent.getUuid(), transactionId, messageTimestamp
            );
        }
        producer.send(Collections.singletonList(message));
    }
}
