package com.ft.methodearticleinternalcomponentsmapper.messaging;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodearticleinternalcomponentsmapper.exception.InvalidMethodeContentException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleUnsupportedSourceCodeException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
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
        } catch (MethodeArticleMarkedDeletedException e) {
            LOGGER.info("Article with uuid={} marked as deleted. Delete message event is created.", methodeContent.getUuid());
            message = messageBuilder.buildDeletedInternalComponentsMessage(methodeContent.getUuid(), transactionId, messageTimestamp);
        } catch (MethodeArticleNotEligibleForPublishException e) {
            LOGGER.error("Article with uuid={} was no eligible for publishing.\n Stack trace was: {}", methodeContent.getUuid(), ExceptionUtils.getStackTrace(e));
            return;
        } catch (MethodeArticleUnsupportedSourceCodeException e) {
            LOGGER.error("Article with uuid={} has unsupported SourceCode for publishing.\n Stack trace was: {}", methodeContent.getUuid(), ExceptionUtils.getStackTrace(e));
            return;
        } catch (InvalidMethodeContentException e) {
            LOGGER.error("Article with uuid={} has content that cannot be transformed.\n Stack trace was: {}", methodeContent.getUuid(), ExceptionUtils.getStackTrace(e));
            return;
        } catch (TransformationException e) {
            LOGGER.error("Article with uuid={} failed to be transformed.\n Stack trace was: {}", methodeContent.getUuid(), ExceptionUtils.getStackTrace(e));
            return;
        }
        producer.send(Collections.singletonList(message));
    }
}
