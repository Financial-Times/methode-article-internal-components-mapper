package com.ft.methodearticleinternalcomponentsmapper.messaging;

import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageProducingInternalComponentsMapperTest {

    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private MessageProducer producer;
    @Mock
    private InternalComponentsMapper mapper;

    private MessageProducingInternalComponentsMapper msgProducingArticleMapper;

    @Before
    public void setUp() {
        msgProducingArticleMapper = new MessageProducingInternalComponentsMapper(
                messageBuilder,
                producer,
                mapper
        );
    }

    @Test
    public void thatMessageIsCreatedFromMappedArticle() {
        Date lastModified = new Date();
        InternalComponents mappedArticle = new InternalComponents.Builder()
                .withUuid(UUID.randomUUID().toString())
                .build();
        when(mapper.map(any(), eq("tid"), eq(lastModified), anyBoolean())).thenReturn(mappedArticle);

        msgProducingArticleMapper.mapInternalComponents(new EomFile.Builder().build(), "tid", lastModified);

        verify(messageBuilder).buildMessage(mappedArticle);
    }

    @Test
    public void thatMessageWithContentIsSentToQueue() {
        InternalComponents mockedContent = mock(InternalComponents.class);
        Message mockedMessage = mock(Message.class);
        when(mapper.map(any(), anyString(), any(), eq(false))).thenReturn(mockedContent);
        when(messageBuilder.buildMessage(mockedContent)).thenReturn(mockedMessage);

        msgProducingArticleMapper.mapInternalComponents(new EomFile.Builder().build(), "tid", new Date());

        verify(producer).send(Collections.singletonList(mockedMessage));
    }

    @Test
    public void thatMessageWithContentMarkedAsDeletedIsSentToQueue() {
        String tid = "tid";
        Date date = new Date();
        String uuid = UUID.randomUUID().toString();
        Message deletedContentMsg = mock(Message.class);
        String contentType = "DynamicContent";
        MethodeMarkedDeletedException mockException = mock(MethodeMarkedDeletedException.class);
        when(mockException.getType()).thenReturn(contentType);

        when(mapper.map(any(), anyString(), any(), eq(false))).thenThrow(mockException);
        when(messageBuilder.buildDeletedInternalComponentsMessage(uuid, tid, date, contentType)).thenReturn(deletedContentMsg);

        msgProducingArticleMapper.mapInternalComponents(new EomFile.Builder().withUuid(uuid).build(), tid, date);

        verify(producer).send(Collections.singletonList(deletedContentMsg));
    }
}