package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MethodeVideoXmlEventHandlerTest extends BaseXMLEventHandlerTest {

    private static final String VIDEO_PLAYER_ELEMENT = "videoPlayer";
    private static final String VIDEO_ID_ATTRIBUTE_NAME = "videoID";
    private static final String VIDEO_ID = "3920663836001";
    private static final String VIDEO_UUID = "e21e5235-5f3f-4a53-bafd-73dbcd0552e0";
    private static final String DATA_EMBEDDED = "data-embedded";
    private static final String VIDEO_TYPE = "http://www.ft.com/ontology/content/Video";
    private static final String FT_CONTENT_TAG = "ft-content";
    private static final String ID = "id";
    private static final String TYPE = "type";

    private MethodeVideoXmlEventHandler eventHandler;
    @Mock
    private XMLEventHandler fallbackHandler;
    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;

    @Before
    public void setup() {
        eventHandler = new MethodeVideoXmlEventHandler(VIDEO_ID_ATTRIBUTE_NAME, fallbackHandler);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementVideoIdAttributeValuesAreNull() throws Exception {
        StartElement startElement = getStartElement(VIDEO_PLAYER_ELEMENT);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementVideoIdAttributeValuesAreEmpty() throws Exception {
        StartElement startElement = getStartElementWithAttributes(VIDEO_PLAYER_ELEMENT,
                buildOneAttributeMap("", ""));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldWriteTransformedElementsToWriterWhenBrightcoveId() throws Exception {
        Map<String, String> transformedAttributes = new HashMap<>();
        transformedAttributes.put(ID, UUID.nameUUIDFromBytes(VIDEO_ID.getBytes()).toString());
        transformedAttributes.put(DATA_EMBEDDED, Boolean.TRUE.toString());
        transformedAttributes.put(TYPE, VIDEO_TYPE);
        StartElement startElement = getStartElementWithAttributes(VIDEO_PLAYER_ELEMENT,
                buildOneAttributeMap(VIDEO_ID_ATTRIBUTE_NAME, VIDEO_ID));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(FT_CONTENT_TAG, transformedAttributes);
        verify(mockBodyWriter).writeEndTag(FT_CONTENT_TAG);
    }

    @Test
    public void shouldWriteTransformedElementsToWriterWhenNextVideoUUID() throws Exception {
        Map<String, String> transformedAttributes = new HashMap<>();
        transformedAttributes.put(ID, VIDEO_UUID);
        transformedAttributes.put(DATA_EMBEDDED, Boolean.TRUE.toString());
        transformedAttributes.put(TYPE, VIDEO_TYPE);
        StartElement startElement = getStartElementWithAttributes(VIDEO_PLAYER_ELEMENT,
                buildOneAttributeMap(VIDEO_ID_ATTRIBUTE_NAME, VIDEO_UUID));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(FT_CONTENT_TAG, transformedAttributes);
        verify(mockBodyWriter).writeEndTag(FT_CONTENT_TAG);
    }

    private Map<String, String> buildOneAttributeMap(String attributeName, String attributeValue) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(attributeName, attributeValue);
        return attributes;
    }
}
