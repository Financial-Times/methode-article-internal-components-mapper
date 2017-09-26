package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlideshowEventHandlerTest extends BaseXMLEventHandlerTest {

    private SlideshowEventHandler eventHandler;

    private static final String START_ELEMENT_TAG = "a";
    private static final String INCORRECT_TAG_NAME = "g";
    private static final String HREF_ATTRIBUTE_NAME = "href";
    private static final String SLIDESHOW_URL_TEMPLATE = "http://www.ft.com/cms/s/null.html#slide0";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_VALUE = "slideshow";
    private static final String TITLE_STRING = "Type title";

    @Mock
    private XMLEventHandler mockFallbackEventHandler;
    @Mock
    private XmlParser<SlideshowData> mockXmlParser;
    @Mock
    private StartElementMatcher mockElementMatcher;
    @Mock
    private XMLEventReader mockXMLEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private SlideshowData mockSlideshowData;

    @Before
    public void setup() {
        eventHandler = new SlideshowEventHandler(mockXmlParser, mockFallbackEventHandler, mockElementMatcher);
    }

    @Test
    public void shouldUseFallbackHandlerIfMatcherDoesNotMatchStartElement() throws Exception {
        StartElement startElement = getStartElement(INCORRECT_TAG_NAME);
        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockFallbackEventHandler).handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldNotWriteIfIfIfNotAllValidDataIsPresent() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        attributes.put(HREF_ATTRIBUTE_NAME, SLIDESHOW_URL_TEMPLATE);
        StartElement startElement = getStartElementWithAttributes(START_ELEMENT_TAG, attributes);

        when(mockElementMatcher.matches(startElement)).thenReturn(true);
        when(mockXmlParser.parseElementData(startElement, mockXMLEventReader, mockBodyProcessingContext)).thenReturn(mockSlideshowData);
        when(mockSlideshowData.isAllRequiredDataPresent()).thenReturn(false);

        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter, times(0)).writeStartTag(START_ELEMENT_TAG, attributes);
        verify(mockBodyWriter, times(0)).writeEndTag(START_ELEMENT_TAG);
    }

    @Test
    public void shouldWriteTransformedElementsToWriter() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        attributes.put(SlideshowEventHandler.DATA_ASSET_TYPE, SlideshowEventHandler.SLIDESHOW);
        attributes.put(SlideshowEventHandler.DATA_EMBEDDED, SlideshowEventHandler.YEP);
        attributes.put(SlideshowEventHandler.TITLE, TITLE_STRING);
        attributes.put(HREF_ATTRIBUTE_NAME, SLIDESHOW_URL_TEMPLATE);
        StartElement startElement = getStartElementWithAttributes(START_ELEMENT_TAG, attributes);

        when(mockElementMatcher.matches(startElement)).thenReturn(true);
        when(mockXmlParser.parseElementData(startElement, mockXMLEventReader, mockBodyProcessingContext)).thenReturn(mockSlideshowData);
        when(mockSlideshowData.getTitle()).thenReturn(TITLE_STRING);
        when(mockSlideshowData.isAllRequiredDataPresent()).thenReturn(true);

        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(START_ELEMENT_TAG, attributes);
        verify(mockBodyWriter).writeEndTag(START_ELEMENT_TAG);
    }
}
