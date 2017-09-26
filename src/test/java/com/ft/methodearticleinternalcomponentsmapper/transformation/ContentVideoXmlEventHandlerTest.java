package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ContentVideoXmlEventHandlerTest extends BaseXMLEventHandlerTest {

    private ContentVideoXmlEventHandler eventHandler;
    @Mock
    private XMLEventHandler fallbackHandler;

    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;

    private static final String VIDEO_LINK_ELEMENT = "ft-content";
    private static final String VIDEO_ID_HOLDER_ATTRIBUTE_NAME = "href";
    private static final String VIDEO_UUID = "e21e5235-5f3f-4a53-bafd-73dbcd0552e0";
    private static final String VIDEO_ID_HOLDER = "/FT/Content/Links/Videos/Video_upload-test.mp4_" + VIDEO_UUID
            + ".xml?uuid=dcc02264-2438-11e7-a24c-6bbb6ec0bc98&amp;xp=/doc/story/text/body&amp;lt=link\" id=\"U11603526769084aUD\"";
    private static final String VIDEO_ID_HOLDER_NO_UUID = "/FT/Content/Links/Videos/Video_upload-test.mp4_123456789.xml?uuid=dcc02264-2438-11e7-a24c-6bbb6ec0bc98&amp;xp=/doc/story/text/body&amp;lt=link\" id=\"U11603526769084aUD\"";
    private static final String DATA_EMBEDDED = "data-embedded";
    private static final String VIDEO_TYPE = "http://www.ft.com/ontology/content/Video";
    private static final String CONTENT_TAG = "content";
    private static final String ID = "id";
    private static final String TYPE = "type";

    @Before
    public void setup() throws Exception {
        eventHandler = new ContentVideoXmlEventHandler(VIDEO_ID_HOLDER_ATTRIBUTE_NAME, fallbackHandler);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementVideoIdHolderAttributeValueIsMissing() throws Exception {
        StartElement startElement = getStartElement(VIDEO_LINK_ELEMENT);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementVideoIdHolderAttributeValueIsEmpty() throws Exception {
        StartElement startElement = getStartElementWithAttributes(
                VIDEO_LINK_ELEMENT, buildOneAttributeMap(VIDEO_ID_HOLDER_ATTRIBUTE_NAME, ""));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldWriteTransformedElementsToWriterWhenNextVideoUUID() throws Exception {
        Map<String, String> transformedAttributes = new HashMap<>();
        transformedAttributes.put(ID, VIDEO_UUID);
        transformedAttributes.put(DATA_EMBEDDED, Boolean.TRUE.toString());
        transformedAttributes.put(TYPE, VIDEO_TYPE);
        StartElement startElement = getStartElementWithAttributes(
                VIDEO_ID_HOLDER_ATTRIBUTE_NAME, buildOneAttributeMap(VIDEO_ID_HOLDER_ATTRIBUTE_NAME, VIDEO_ID_HOLDER));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(CONTENT_TAG, transformedAttributes);
        verify(mockBodyWriter).writeEndTag(CONTENT_TAG);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementVideoIdHolderAttributeValueIsNotUUID() throws Exception {
        StartElement startElement = getStartElementWithAttributes(VIDEO_ID_HOLDER_ATTRIBUTE_NAME,
                buildOneAttributeMap(VIDEO_ID_HOLDER_ATTRIBUTE_NAME, VIDEO_ID_HOLDER_NO_UUID));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    private Map<String, String> buildOneAttributeMap(String attributeName, String attributeValue) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(attributeName, attributeValue);
        return attributes;
    }
}
